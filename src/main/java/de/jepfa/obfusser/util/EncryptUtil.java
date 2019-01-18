package de.jepfa.obfusser.util;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;

/**
 * Utils to help with en-/decrypt data and generate keys from user secrets like passwords or pins.
 *
 * @author Jens Pfahl
 */
public class EncryptUtil {

    private static final int BYTE_COUNT = 256;


    /*
     * To encypt single chars, we need to define which chars are common in credentials.
     */
    static final Character[] USED_CHARS = new Character[]{
            '0','1','2','3','4','5','6','7','8','9',
            ' ','!','"','§','$','%','&','/','(',')','=','?','`','´','+','#','-','.',',','<','>',';',
            ':','_','\'','*','¡','“','^','°','¢','[',']','|','{','}','¿','–','@','€'};
    /*
     * We also add all possible letters.
     */
    static final List<Character> CHARACTERS = new ArrayList<>(Arrays.asList(USED_CHARS));
    static {
        for (int i = 0; i < BYTE_COUNT; i++) {
            char c = (char) i;

            if (Character.isLetter(c)) {
                CHARACTERS.add(c);
            }
        }
    }


    static final Loop<Character> LOOP_ENCRYPT_CHARS = new Loop<>(CHARACTERS);


    /**
     * Generates a key as byte array from a user secret like password or pin.
     *
     * @param pin
     * @return
     */
    public static byte[] generateKey(String pin, byte[] salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(pin.getBytes());
            if (salt != null) {
                messageDigest.update(salt);
            }
            byte[] digest = messageDigest.digest();
 //           Log.e("KEY", Arrays.toString(digest));
            return digest;
        } catch (NoSuchAlgorithmException e) {
            Log.e("KEY", "Cannot get ", e);
            e.printStackTrace();
        }

        return null;
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
                char c =  s.charAt(i);
                int b = key[(index + i) % key.length];

                if (LOOP_ENCRYPT_CHARS.applies(c)) {
                    Character encryptedChar = LOOP_ENCRYPT_CHARS.forwards(c, b);
//                    Log.e("DEC_CHAR", "in=" + c + " key=" + b + " out=" + encryptedChar);
                    sb.append(encryptedChar.charValue());
                }
                else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return s;
    }

    /**
     *
     * @param s
     * @param index the encrypted index if possible
     * @param key
     * @return
     */
    public static String decryptHint(String s, int index, byte[] key) {
        if (s != null && !s.isEmpty() && key != null) {
            StringBuilder sb = new StringBuilder();
//            Log.e("ENC_CHAR", "s=" + s +" index=" + index + " key=" + Arrays.toString(key));

            for (int i = 0; i < s.length() && i < key.length; i++) {
                char c =  s.charAt(i);
                int b = key[(index + i) % key.length];

                if (LOOP_ENCRYPT_CHARS.applies(c)) {
                    Character encryptedChar = LOOP_ENCRYPT_CHARS.backwards(c, b);
//                    Log.e("ENC_CHAR", "in=" + c + " key=" + b + " out=" + encryptedChar);
                    sb.append(encryptedChar.charValue());
                }
                else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        return s;
    }

    public static int encryptIndex(int index, int patternLength, byte[] key) {
        if (key == null) {
            return index;
        }
        int k = getKeyForIndex(key);

        return (index + k) % patternLength;
    }

    public static int decryptIndex(int index, int patternLength, byte[] key) {
        if (key == null) {
            return index;
        }
        int k = getKeyForIndex(key);

        int forward = patternLength - (k % patternLength);
        return (index + forward) % patternLength;
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


}
