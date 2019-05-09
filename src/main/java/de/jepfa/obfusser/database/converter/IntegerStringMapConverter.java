package de.jepfa.obfusser.database.converter;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import de.jepfa.obfusser.util.encrypt.DbCrypt;

public class IntegerStringMapConverter {

    @TypeConverter
    public static Map<Integer, String> restoreMap(String listOfString) {
        return new Gson().fromJson(
                DbCrypt.aesDecrypt(listOfString),
                new TypeToken<Map<Integer, String>>() {
                }.getType());
    }

    @TypeConverter
    public static String saveMap(Map<Integer, String> listOfString) {
        String serialized = new Gson().toJson(listOfString);
        return DbCrypt.aesEncrypt(serialized);
    }
}
