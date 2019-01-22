package de.jepfa.obfusser.util;

import android.support.v4.util.Pair;

import org.junit.Assert;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.model.Representation;
import de.jepfa.obfusser.model.SecurePatternHolder;

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

        for (int i = 0; i < 10000; i++) {

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

        String string = "abcdefghiABCDEFG!§$/138";

        for (int i = 0; i < 10000; i++) {
            char[] pwd = String.valueOf(i).toCharArray();
            byte[] salt = null;
            byte[] key = EncryptUtil.generateKey(pwd, salt);

            String encrypted = EncryptUtil.encryptHint(string, 23, key);
            String decrypted = EncryptUtil.decryptHint(encrypted, 23, key);

            System.out.println(pwd + ": " + string + " --> " + encrypted + " --> " + decrypted);

            Assert.assertEquals(string, decrypted);
        }
    }

    @Test
    public void encryptDecryptPattern() throws Exception {
        SecurePatternHolder pattern = new Credential();
        pattern.setPatternFromUser("pa$Sw0rd", null);
        pattern.setHint(2, "hint1", null);
        pattern.setHint(3, "hint2", null);
        String originalPattern = pattern.toString();

        byte[] key = EncryptUtil.generateKey("1234".toCharArray(), null);
        pattern.encrypt(key);
        System.out.println(pattern);
        pattern.decrypt(key);
        System.out.println(pattern);

        String decryptedPattern = pattern.toString();

        Assert.assertEquals(originalPattern, decryptedPattern);

    }

    @Test
    public void encryptDecryptIndex() throws Exception {
        byte[] key = EncryptUtil.generateKey("1234".toCharArray(), null);
        System.out.println("k=" + EncryptUtil.getKeyForIndex(key));
        final int PATTERN_LENGTH = 5;
        int encryptedIndex = EncryptUtil.encryptIndex(0, PATTERN_LENGTH, key);

        Assert.assertEquals(2, encryptedIndex);

        int decryptedIndex = EncryptUtil.decryptIndex(encryptedIndex, PATTERN_LENGTH, key);

        Assert.assertEquals(0, decryptedIndex);
    }


    @Test
    public void encryptDecryptRandomIndexes() throws Exception {
        Random r = new Random();
        for (int i = 0; i < 10000; i++) {

            byte[] key = EncryptUtil.generateKey(String.valueOf(i).toCharArray(), null);
            int patternLength = r.nextInt(18) + 1;
            int originIndex = r.nextInt(patternLength);

            int encryptedIndex = EncryptUtil.encryptIndex(originIndex, patternLength, key);
            int decryptedIndex = EncryptUtil.decryptIndex(encryptedIndex, patternLength, key);
            System.out.println("k(" + i + ")=" + EncryptUtil.getKeyForIndex(key) + ", length=" + patternLength + ", i=" + originIndex + "-->" + encryptedIndex + "-->" + decryptedIndex);

            Assert.assertEquals(originIndex, decryptedIndex);
        }
    }

}
