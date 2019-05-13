package de.jepfa.obfusser.util.encrypt;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.util.Log;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import de.jepfa.obfusser.Constants;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.util.encrypt.hints.EncryptedHintChar;
import de.jepfa.obfusser.util.encrypt.hints.HintChar;

/**
 * Utils to help with en-/decrypt data and generate keys from user secrets like passwords or pins.
 *
 * @author Jens Pfahl
 */
public class EncryptUtil {

    private static final int BYTE_COUNT = 1 << Byte.SIZE;
    private static final String CIPHER_AES_GCM = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    /*
     * To encypt special chars, we need to define which chars are common in credentials.
     */
    static final Character[] KNOWN_SPECIAL_CHARS = new Character[]{
            ' ','!','"','§','$','%','&','/','(',')','=','?','`','´','+','#','-','.',',','<','>',';',
            ':','_','\'','\\','*','¡','“','^','°','¢','[',']','|','{','}','¿','–','@','€'};

    /*
     * We also add all possible digits and letters.
     */
    static final List<HintChar> CHARACTERS = new ArrayList<>();
    static {
        // add special chars
        for (Character c : Arrays.asList(KNOWN_SPECIAL_CHARS)) {
            CHARACTERS.add(new HintChar(c, true));
        }

        // add letters and digits
        for (int i = 0; i < BYTE_COUNT; i++) {
            char c = (char) i;

            if (Character.isDigit(c)) {
                CHARACTERS.add(new HintChar(c, false));
            }
            else if (Character.isLetter(c)) {
                boolean isCommonLetter =
                        (c >= 'a' && c <= 'z') ||
                        (c >= 'A' && c <= 'Z');
                CHARACTERS.add(new HintChar(c, !isCommonLetter));
            }
        }
    }

    static final Loop<HintChar> LOOP_ENCRYPT_CHARS = new Loop<>(CHARACTERS);

    private static final ConcurrentMap<String, KeyStore> keyStoreMap = new ConcurrentHashMap<>();


    /**
     * Does AES encryption for the given data. Uses the alias to provide a save encryption key managed by Android.
     * Only supported for Android M and greater.
     *
     * @param alias
     * @param data
     * @return a Pair containing first the init vector and second the encrypted data or null in case off error
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static Pair<byte[],byte[]> encryptData(final String alias, final byte[] data) {

        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_AES_GCM);
            SecretKey androidSecretKey = getAndroidSecretKey(alias);
            if (androidSecretKey == null) {
                Log.e("ENCDATA", "Key is null: " + alias);
                return null;
            }

            cipher.init(Cipher.ENCRYPT_MODE, androidSecretKey);

          return new Pair<>(cipher.getIV(), cipher.doFinal(data));
        } catch (Exception e) {
            Log.e("ENCDATA", "Encryption error wth alias= " + alias, e);
        }

        return null;
    }




    /**
     * Does AES decryption for the given init vector and data. Uses the alias to provide a save encryption key managed by Android.
     * Only supported for Android M and greater.
     *
     * @param alias
     * @param encryptedIvAndData
     * @return the decrypted data or null in case off error
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static byte[] decryptData(final String alias, Pair<byte[], byte[]> encryptedIvAndData) {

        try {
            byte[] encryptionIv = encryptedIvAndData.first;
            byte[] encryptedData = encryptedIvAndData.second;

            SecretKey secretKey = findStoredKey(alias);
            if (secretKey == null) {
                Log.e("DECDATA", "No key found for: " + alias);
            }

            final Cipher cipher = Cipher.getInstance(CIPHER_AES_GCM);
            final GCMParameterSpec spec = new GCMParameterSpec(128, encryptionIv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return cipher.doFinal(encryptedData);

        } catch (Exception e) {
            Log.e("DECDATA", "Decryption error wth alias= " + alias, e);
        }

        return null;
    }

    private static SecretKey findStoredKey(String alias) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException {

        KeyStore keyStore;
        if (!keyStoreMap.containsKey(alias)) {
            synchronized (alias) {
                keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                keyStoreMap.putIfAbsent(alias, keyStore);
            }
        }
        keyStore = keyStoreMap.get(alias);

        KeyStore.Entry entry = keyStore.getEntry(alias, null);
        if (entry != null) {
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        }
        return null;
    }

    public static boolean isPasswdEncryptionSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * clear passwd in memory
     *
     * @param pwd
     */
    public static void clearPwd(char[] pwd) {
        if (pwd != null) {
            Arrays.fill(pwd, (char) 0);
        }
    }

    /**
     * Generates a key as byte array from a user secret like password or pin.
     *
     * @param pwd
     * @return
     */
    public static byte[] generateKey(char[] pwd, byte[] salt) {
        PBEKeySpec spec = null;
        try {
            spec = new PBEKeySpec(pwd, salt, 5000, Constants.KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); //PBKDF2WithHmacSHA256 would be better but is not supported until APIL 26+
            byte[] hash = factory.generateSecret(spec).getEncoded();
            //Log.d("GENKEY", Arrays.toString(hash));
            return hash;
        } catch (Exception e) {
            Log.e("GENKEY", "Cannot generate key ", e);
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }

        return null;
    }

    /**
     * Hashes (SHA-512) the given data with the given salt.
     * @param data
     * @param salt
     * @return
     */
    public static byte[] fastHash(byte[] data, byte[] salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(data);
            if (salt != null) {
                messageDigest.update(salt);
            }
            byte[] digest = messageDigest.digest();
            //Log.d("FSTHSH", Arrays.toString(digest));
            return digest;
        } catch (NoSuchAlgorithmException e) {
            Log.e("FSTHSH", "Programming error", e);
        }

        return null;
    }

    /**
     * Generates a unique single key for the given general key and the uuid.
     *
     * @param key
     * @param uuid
     * @return
     */
    public static byte[] generateUuidKey(byte[] key, String uuid) {
        if (uuid == null) {
            return key;
        }
        byte[] salt = uuid.getBytes();
        return fastHash(key, salt);
    }


    /**
     * Transform the given key to a possible likely {@link ObfusString}.
     * This is important to make it hard to guess, which {@link ObfusString} is real and which not.
     *
     * @param key
     * @return
     */
    public static ObfusString keyToObfusString(@NonNull byte[] key) {
        List<ObfusChar> obfusChars = new ArrayList<>(key.length);
        for (byte b : key) {
            double left = 0;
            double right = 0;

            if (isInRange(b, left, right += ObfusChar.LOWER_CASE_CHAR.getUseLikelihood())) {
                obfusChars.add(ObfusChar.LOWER_CASE_CHAR);
            }
            else if (isInRange(b, left += ObfusChar.LOWER_CASE_CHAR.getUseLikelihood(),
                    right += ObfusChar.UPPER_CASE_CHAR.getUseLikelihood())) {
                obfusChars.add(ObfusChar.UPPER_CASE_CHAR);
            }
            else if (isInRange(b, left += ObfusChar.UPPER_CASE_CHAR.getUseLikelihood(),
                    right += ObfusChar.DIGIT.getUseLikelihood())) {
                obfusChars.add(ObfusChar.DIGIT);
            }
            else if (isInRange(b, left += ObfusChar.DIGIT.getUseLikelihood(),
                    right += ObfusChar.SPECIAL_CHAR.getUseLikelihood())) {
                obfusChars.add(ObfusChar.SPECIAL_CHAR);
            }
        }
        return new ObfusString(obfusChars);
    }

    /**
     * Encrypts hint data by using the given index and the key to do this as obfuscating as possible.
     *
     * @param s
     * @param index the encrypted index if possible
     * @param key
     * @return
     */
    public static String encryptHint(String s, int index, byte[] key) {
        if (s != null && !s.isEmpty() && key != null) {
            StringBuilder sb = new StringBuilder();
//            Log.e("DEC_CHAR", "s=" + s +" index=" + index + " key=" + Arrays.toString(key));
            for (int i = 0; i < s.length() && i < key.length; i++) {
                char c = s.charAt(i);
                int b = key[(index + i) % key.length]; // ensure legal index

                EncryptedHintChar encryptedHintChar = EncryptedHintChar.ofDecrypted(c);
                if (LOOP_ENCRYPT_CHARS.applies(encryptedHintChar)) {

                    do {
                        HintChar hint = LOOP_ENCRYPT_CHARS.forwards(encryptedHintChar, b);
                        encryptedHintChar.apply(hint);
                    } while (encryptedHintChar.doNext());
//                    Log.e("ENC_CHAR", "in=" + c + " key=" + b + " out=" + encryptedHintChar);
                }
                sb.append(encryptedHintChar.getHintStoreString());
            }
            return sb.toString();
        }
        return s;
    }

    /**
     * Decrypts encrypted hint data by using the given index and the key to do this as obfuscating as possible.
     *
     * @param s
     * @param index the encrypted index if possible
     * @param key
     * @return
     */
    public static String decryptHint(String s, int index, byte[] key) {
        if (s != null && s.length() > 1 && key != null) {
            StringBuilder sb = new StringBuilder();
//            Log.e("ENC_CHAR", "s=" + s +" index=" + index + " key=" + Arrays.toString(key));
            for (int i = 0; i < s.length() / 2 && i < key.length; i++) {
                String decHint = s.substring(i * 2, i * 2 + 2);
                int b = key[(index + i) % key.length]; // ensure legal index
                EncryptedHintChar decryptedHint = EncryptedHintChar.ofEncrypted(decHint);

                if (LOOP_ENCRYPT_CHARS.applies(decryptedHint)) {
                    int br = b * decryptedHint.getRoundTrips();
                    HintChar encryptedHint = LOOP_ENCRYPT_CHARS.backwards(decryptedHint, br);
//                    Log.e("ENC_CHAR", "in=" + c + " key=" + b + " out=" + encryptedChar);
                    sb.append(encryptedHint.getHint());
                } else {
                    sb.append(decryptedHint.getHint());
                }
            }
            return sb.toString();
        }
        return s;
    }

    /**
     * Encrypts the given index by using the current pattern length and a key.
     *
     * @param index
     * @param patternLength
     * @param key
     * @return
     */
    public static int encryptIndex(int index, int patternLength, byte[] key) {
        if (key == null) {
            return index;
        }
        int k = getKeyForIndex(key);

        return (index + k) % patternLength;
    }

    /**
     * Decrypts the given encrypted index by using the current pattern length and a key.
     *
     * @param index
     * @param patternLength
     * @param key
     * @return
     */
    public static int decryptIndex(int index, int patternLength, byte[] key) {
        if (key == null) {
            return index;
        }
        int k = getKeyForIndex(key);

        int forward = patternLength - (k % patternLength);
        return (index + forward) % patternLength;
    }

    public static char[] getCharArray(Editable editable) {
        if (editable == null) {
            return null;
        }
        int l = editable.length();
        char[] chararray = new char[l];
        editable.getChars(0, l, chararray, 0);
        return chararray;
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[Constants.KEY_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    static int getKeyForIndex(byte[] key) {
        return Math.abs(key[key.length - 1]);
    }

    private static boolean isInRange(byte b, double left, double right) {
        double zeroBased = b + -Byte.MIN_VALUE; // [-128 ... 127] --> [0 ... 255]

        // b/256 = ul/100
        double i = zeroBased / BYTE_COUNT;

        return left <= i && i < right;
    }


    /**
     * Returns null if not target api
     * @param alias
     * @return
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static SecretKey getAndroidSecretKey(final String alias) throws Exception {

        if (isPasswdEncryptionSupported()) {
            SecretKey secretKey = findStoredKey(alias);
            if (secretKey == null) {
                synchronized (alias) {
                    // check after sync block entered
                    secretKey = findStoredKey(alias);
                    if (secretKey != null) {
                        return secretKey;
                    }
                    KeyGenerator keyGenerator = KeyGenerator
                            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

                    keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());

                    secretKey = keyGenerator.generateKey();
                }
            }
            return secretKey;
        }

        return null;
    }


}
