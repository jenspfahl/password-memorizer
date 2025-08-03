package de.jepfa.obfusser.util.encrypt;

import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.Log;

import java.util.Arrays;

public class DbCrypt {

    private static final String KEY_DB_CRYPT = "key_db_crypt";
    private static final String CRYPTED_CONTENT_INDICATOR = "_vD+";
    private static final String BAS64_PAIR_DELIMITOR = ":";

    public static String aesEncrypt(String string) {
        if (string == null) {
            return null;
        }
        Pair<byte[], byte[]> data = EncryptUtil.encryptData(KEY_DB_CRYPT, string.getBytes());
        if (data == null) {
            Log.e("AES-ENC","Cannot encrypt given string");
            return string;
        }
        return CRYPTED_CONTENT_INDICATOR + dataToString(data);
    }

    public static String aesDecrypt(String string) {
        if (string == null) {
            return null;
        }
        if (string.startsWith(CRYPTED_CONTENT_INDICATOR)) {
            Pair<byte[], byte[]> data = stringToData(string.substring(CRYPTED_CONTENT_INDICATOR.length()));
            String dec = new String(EncryptUtil.decryptData(KEY_DB_CRYPT, data));
            if (dec == null) {
                Log.e("AES-DEC", "Cannot decrypt given string");
                return string;
            }
            return dec;
        }
        return string;
    }

    private static String dataToString(Pair<byte[],byte[]> data) {
        String first = Base64.encodeToString(data.first, Base64.NO_WRAP|Base64.NO_PADDING);
        String second = Base64.encodeToString(data.second, Base64.NO_WRAP|Base64.NO_PADDING);
        return first + BAS64_PAIR_DELIMITOR + second;
    }

    private static Pair<byte[],byte[]> stringToData(String string) {
        String[] splitted = string.split(BAS64_PAIR_DELIMITOR);
        byte[] first = Base64.decode(splitted[0], Base64.NO_WRAP|Base64.NO_PADDING);
        byte[] second = Base64.decode(splitted[1], Base64.NO_WRAP|Base64.NO_PADDING);

        return new Pair<>(first, second);
    }
}
