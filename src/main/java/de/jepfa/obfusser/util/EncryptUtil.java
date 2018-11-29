package de.jepfa.obfusser.util;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jepfa.obfusser.model.ObfusChar;
import de.jepfa.obfusser.model.ObfusString;
import de.jepfa.obfusser.ui.settings.SettingsActivity;

/**
 * Utils to help with en-/decrypt data and generate keys from user secrets like passwords or pins.
 *
 * @author Jens Pfahl
 */
public class EncryptUtil {

    public static final int BYTE_COUNT = 256;

    /**
     * To encypt single chars, we need to define which chars are common in credentials.
     */
    private static final Character[] USED_CHARS = new Character[]{
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            '0','1','2','3','4','5','6','7','8','9',
            ' ','!','"','§','$','%','&','/','(',')','=','?','`','´','+','#','-','.',',','<','>',';',
            ':','_','\'','*','¡','“','^','°','¢','[',']','|','{','}','≠','¿','∞','…','–','@','€'};
    private static final Loop<Character> LOOP_ENCRYPT_CHARS = new Loop<>(Arrays.asList(USED_CHARS));


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
                //messageDigest.update(salt); TODO activate this when it is useful
            }
            return messageDigest.digest();
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

    public static String encryptPlainString(String s, byte[] key) {
        if (s != null && !s.isEmpty() && key != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length() && i < key.length; i++) {
                char c =  s.charAt(i);
                int b = Math.abs(key[i]);

                if (LOOP_ENCRYPT_CHARS.applies(c)) {
                    Character encryptedChar = LOOP_ENCRYPT_CHARS.forwards(c, b);
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

    public static String decryptPlainString(String s, byte[] key) {
        if (s != null && !s.isEmpty() && key != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length() && i < key.length; i++) {
                char c =  s.charAt(i);
                int b = Math.abs(key[i]);

                if (LOOP_ENCRYPT_CHARS.applies(c)) {
                    Character encryptedChar = LOOP_ENCRYPT_CHARS.backwards(c, b);
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

    private static boolean isInRange(byte b, double left, double right) {
        double zeroBased = b + -Byte.MIN_VALUE; // [-128 ... 127] --> [0 ... 255]

        // b/256 = ul/100
        double i = zeroBased / BYTE_COUNT;

        return left <= i && i < right;
    }

}
