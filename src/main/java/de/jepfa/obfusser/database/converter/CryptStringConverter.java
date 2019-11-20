package de.jepfa.obfusser.database.converter;

import android.arch.persistence.room.TypeConverter;

import de.jepfa.obfusser.model.CryptString;
import de.jepfa.obfusser.util.encrypt.DbCrypt;

public class CryptStringConverter {

    @TypeConverter
    public static CryptString restore(String string) {
        if ("".equals(string)) {
            return CryptString.of(string, string);
        }
        String aesString = DbCrypt.INSTANCE.aesDecrypt(string);
        if (aesString != null) {
            return CryptString.of(aesString, string);
        }
        return null;
    }

    @TypeConverter
    public static String save(CryptString cryptString) {
        if (cryptString != null) {
            if ("".equals(cryptString.toString())) {
                return cryptString.toString();
            }
            else {
                String encrypted = DbCrypt.INSTANCE.aesEncrypt(CryptString.from(cryptString));
                return encrypted;
            }
        }
        return null;
    }
}
