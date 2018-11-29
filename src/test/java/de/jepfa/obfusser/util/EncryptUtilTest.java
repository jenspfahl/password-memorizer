package de.jepfa.obfusser.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.model.SecurePatternHolder;

public class EncryptUtilTest {

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

            String pin = String.valueOf(i);
            byte[] salt = UUID.randomUUID().toString().getBytes();
            byte[] key = EncryptUtil.generateKey(pin, salt);
            String origin = user.toRepresentation();
            ObfusString encryptedOS = user.encrypt(key);
            String encrypted = encryptedOS.toRepresentation();
            String encryptedExchangeFormat = encryptedOS.toExchangeFormat();
            String decrypted = user.decrypt(key).toRepresentation();
            System.out.println(pin + ": " + origin + " --> " + encrypted + "(" + encryptedExchangeFormat + ")");

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
    public void encryptDecryptPlainStrings() throws Exception {

        String string = "abcdefghiABCDEFG!§$/138";

        for (int i = 0; i < 10000; i++) {
            String pin = String.valueOf(i);
            byte[] salt = null;
            byte[] key = EncryptUtil.generateKey(pin, salt);

            String encrypted = EncryptUtil.encryptPlainString(string, key);
            String decrypted = EncryptUtil.decryptPlainString(encrypted, key);

            System.out.println(pin + ": " + string + " --> " + encrypted + " --> " + decrypted);

            Assert.assertEquals(string, decrypted);
        }
    }

    @Test
    public void encryptDecryptPattern() throws Exception {
        SecurePatternHolder pattern = new Credential();
        pattern.setPatternFromUser("pa$Sw0rd", null);
        pattern.setPotentialHint(2, "test", null);
        String originalPattern = pattern.toString();

        byte[] key = EncryptUtil.generateKey("1234", null);
        pattern.encrypt(key);
        System.out.println(pattern);
        pattern.decrypt(key);
        System.out.println(pattern);

        String decryptedPattern = pattern.toString();

        Assert.assertEquals(originalPattern, decryptedPattern);

    }

}
