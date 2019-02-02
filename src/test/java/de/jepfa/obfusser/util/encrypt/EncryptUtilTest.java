package de.jepfa.obfusser.util.encrypt;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.model.SecurePatternHolder;
import de.jepfa.obfusser.util.encrypt.hints.HintChar;

public class EncryptUtilTest {


    @Test
    public void showCharLoop() throws Exception {
        System.out.println(EncryptUtil.CHARACTERS);
    }

    @Test
    public void addObfusChar() throws Exception {

        Assert.assertEquals(ObfusChar.UPPER_CASE_CHAR, ObfusChar.UPPER_CASE_CHAR.encrypt((byte) ObfusChar.LOWER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.DIGIT, ObfusChar.UPPER_CASE_CHAR.encrypt((byte) ObfusChar.UPPER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.SPECIAL_CHAR, ObfusChar.UPPER_CASE_CHAR.encrypt((byte) ObfusChar.DIGIT.ordinal()));
        Assert.assertEquals(ObfusChar.LOWER_CASE_CHAR, ObfusChar.UPPER_CASE_CHAR.encrypt((byte) ObfusChar.SPECIAL_CHAR.ordinal()));

        Assert.assertEquals(ObfusChar.DIGIT, ObfusChar.DIGIT.encrypt((byte) ObfusChar.LOWER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.SPECIAL_CHAR, ObfusChar.DIGIT.encrypt((byte) ObfusChar.UPPER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.LOWER_CASE_CHAR, ObfusChar.DIGIT.encrypt((byte) ObfusChar.DIGIT.ordinal()));
        Assert.assertEquals(ObfusChar.UPPER_CASE_CHAR, ObfusChar.DIGIT.encrypt((byte) ObfusChar.SPECIAL_CHAR.ordinal()));

    }

    @Test
    public void substObfusChar() throws Exception {

        Assert.assertEquals(ObfusChar.UPPER_CASE_CHAR, ObfusChar.UPPER_CASE_CHAR.decrypt((byte) ObfusChar.LOWER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.LOWER_CASE_CHAR, ObfusChar.UPPER_CASE_CHAR.decrypt((byte) ObfusChar.UPPER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.SPECIAL_CHAR, ObfusChar.UPPER_CASE_CHAR.decrypt((byte) ObfusChar.DIGIT.ordinal()));
        Assert.assertEquals(ObfusChar.DIGIT, ObfusChar.UPPER_CASE_CHAR.decrypt((byte) ObfusChar.SPECIAL_CHAR.ordinal()));

        Assert.assertEquals(ObfusChar.DIGIT, ObfusChar.DIGIT.decrypt((byte) ObfusChar.LOWER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.UPPER_CASE_CHAR, ObfusChar.DIGIT.decrypt((byte) ObfusChar.UPPER_CASE_CHAR.ordinal()));
        Assert.assertEquals(ObfusChar.LOWER_CASE_CHAR, ObfusChar.DIGIT.decrypt((byte) ObfusChar.DIGIT.ordinal()));
        Assert.assertEquals(ObfusChar.SPECIAL_CHAR, ObfusChar.DIGIT.decrypt((byte) ObfusChar.SPECIAL_CHAR.ordinal()));

    }

    @Test
    public void encryptDecryptObfusStrings() throws Exception {

        for (int i = 0; i < 100; i++) {

            ObfusString user = ObfusString.fromExchangeFormat("0!xxxX?0!X0");

            char[] pwd = String.valueOf(i).toCharArray();
            byte[] salt = UUID.randomUUID().toString().getBytes();
            byte[] key = EncryptUtil.generateKey(pwd, salt);
            String origin = user.toRepresentation(Representation.DEFAULT_BLOCKS);
            ObfusString encryptedOS = user.encrypt(key);
            String encrypted = encryptedOS.toRepresentation(Representation.DEFAULT_BLOCKS);
            String encryptedExchangeFormat = encryptedOS.toExchangeFormat();
            String decrypted = user.decrypt(key).toRepresentation(Representation.DEFAULT_BLOCKS);
            System.out.println(pwd + ": " + origin + " --> " + encrypted + "(" + encryptedExchangeFormat + ")");

            Assert.assertEquals(origin, decrypted);
        }

    }


    @Test
    public void keyToObfusString() throws Exception {

        byte[] bytes = new byte[128];
        for (int i = 0; i < 128; i++) {
            bytes[i] = (byte) ((i * 2 - 128));
        }
        ObfusString obfusString = EncryptUtil.keyToObfusString(bytes);

        Assert.assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX00000000000000000000000000!!!!!!!!!!!!",
                obfusString.toExchangeFormat());

    }

    @Test
    public void encryptDecryptHints() throws Exception {

        byte[] salt = new SecureRandom().generateSeed(32);

        int i = 0;
        List<HintChar> characters = EncryptUtil.CHARACTERS;
        characters.add(0, new HintChar('՜'));
        for (HintChar hintChar : characters) {
            String hint = String.valueOf(hintChar.getHint());
            char[] pwd = String.valueOf(i).toCharArray();
            byte[] key = EncryptUtil.generateKey(pwd, salt);

            String encrypted = EncryptUtil.encryptHint(hint, 23, key);
            String decrypted = EncryptUtil.decryptHint(encrypted, 23, key);

            System.out.println(i + ") k=" + key[23] + ": " + hint + " --> " + encrypted + " --> " + decrypted);

            Assert.assertEquals(hint, decrypted);
            i++;
        }
    }

    @Test
    public void encryptDecryptPattern() throws Exception {
        SecurePatternHolder pattern = createCredential(null, "pa$Sw0rd", 2, "hint1");
        pattern.setHint(3, "hint2", null, true);
        pattern.setUuid("uuid");
        String originalPattern = pattern.toString();

        byte[] key = EncryptUtil.generateKey("1234".toCharArray(), new SecureRandom().generateSeed(32));
        pattern.encrypt(key, true);
        System.out.println(pattern);
        pattern.decrypt(key, true);
        System.out.println(pattern);

        pattern.setUuid("uuid");
        String decryptedPattern = pattern.toString();

        Assert.assertEquals(originalPattern, decryptedPattern);

    }

    @Test
    public void encryptDecryptIndex() throws Exception {
        byte[] key = EncryptUtil.generateKey("1234".toCharArray(), new SecureRandom().generateSeed(32));
        System.out.println("k=" + EncryptUtil.getKeyForIndex(key));
        final int PATTERN_LENGTH = 5;
        int encryptedIndex = EncryptUtil.encryptIndex(0, PATTERN_LENGTH, key);
        int decryptedIndex = EncryptUtil.decryptIndex(encryptedIndex, PATTERN_LENGTH, key);

        Assert.assertEquals(0, decryptedIndex);
    }


    @Test
    public void encryptDecryptRandomIndexes() throws Exception {
        Random r = new Random();
        byte[] salt = new SecureRandom().generateSeed(32);
        for (int i = 0; i < 100; i++) {

            byte[] key = EncryptUtil.generateKey(String.valueOf(i).toCharArray(), salt);
            int patternLength = r.nextInt(18) + 1;
            int originIndex = r.nextInt(patternLength);

            int encryptedIndex = EncryptUtil.encryptIndex(originIndex, patternLength, key);
            int decryptedIndex = EncryptUtil.decryptIndex(encryptedIndex, patternLength, key);
            System.out.println("k(" + i + ")=" + EncryptUtil.getKeyForIndex(key) + ", length=" + patternLength + ", i=" + originIndex + "-->" + encryptedIndex + "-->" + decryptedIndex);

            Assert.assertEquals(originIndex, decryptedIndex);
        }
    }

    @Test
    public void genUUIDKeys() throws Exception {
        Random r = new Random();
        byte[] salt = new SecureRandom().generateSeed(32);
        for (int i = 0; i < 100; i++) {

            byte[] key = EncryptUtil.generateKey(String.valueOf(i).toCharArray(), salt);

            byte[] indexKey = EncryptUtil.generateUuidKey(key, String.valueOf(i));
            System.out.println("key(" + i + ")=" + Arrays.toString(key));
            System.out.println("ink(" + i + ")=" + Arrays.toString(indexKey));

            Assert.assertEquals(key.length, indexKey.length);
        }
    }

    @Ignore
    public void tryBruteForce() throws Exception {
        byte[] appSalt = EncryptUtil.generateSalt();
        char[] pwd = "987".toCharArray();
        byte[] key = EncryptUtil.generateKey(pwd, appSalt);

        int credentialCount = 5;
        List<Credential> credentials = createCredentials(credentialCount, key, "abcdeFGH123");


        for (int i = 0; i < 100000; i++) {

            if (i % 10 == 0) {
                System.out.print(".");
                if (i % 100 == 0) {
                    System.out.println(i);
                }
            }
            char[] p = String.valueOf(i).toCharArray();
            byte[] k = EncryptUtil.generateKey(p, appSalt);

            List<String> patterns = getPatterns(credentials, k);


            Map<String, Integer> pCounter = new HashMap<>();
            for (String pattern : patterns) {
                Integer pCount = pCounter.get(pattern);
                if (pCount == null) {
                    pCounter.put(pattern, 1);
                }
                else {
                    pCounter.put(pattern, pCount + 1);
                }
            }

            if (pCounter.size() == 1) {
                System.err.println("all are the same, pwd: " + i);
            }
            else  if (pCounter.size() <= 3) {
              //  System.err.println(pCounter.size() + " candidates, potentional pwd: " + i);
            }
            else {
                //System.err.println(pCounter.size() + " candidates, weak pwd: " + i);
            }
        }
    }

    private List<String> getPatterns(List<Credential> credentials, byte[] k) {
        List<String> p = new ArrayList<>(credentials.size());
        for (Credential credential : credentials) {
            p.add(credential.getPatternAsExchangeFormatHinted(k, true));
        }
        return p;
    }

    private List<Credential> createCredentials(int count, byte[] key, String pattern) {
        List<Credential> credentials = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Credential credential = createCredential(key, pattern, 0, String.valueOf(i));
            System.out.println(Arrays.toString(credential.getUUIDKey(key, true)));
            credentials.add(credential);
        }
        return credentials;
    }

    @NonNull
    private Credential createCredential(byte[] key, String pattern, int i2, String a) {
        Credential credential1 = new Credential();
        credential1.setPatternFromUser(pattern, key, true);
        credential1.setHint(i2, a, key, true);
        return credential1;
    }

}
