package de.jepfa.obfusser.database.converter;

import android.arch.persistence.room.TypeConverter;

import de.jepfa.obfusser.model.CryptString;
import de.jepfa.obfusser.util.encrypt.DbCrypt;

public class CryptStringConverter {

    @TypeConverter
    public static CryptString restore(String string) {
        String aesString = DbCrypt.aesDecrypt(string);
        if (aesString != null) {
            return CryptString.of(aesString);
        }
        return null;
    }

    @TypeConverter
    public static String save(CryptString cryptString) {
        if (cryptString != null) {
            String encrypted = DbCrypt.aesEncrypt(CryptString.from(cryptString));
            return encrypted;
        }
        return null;
    }
}
