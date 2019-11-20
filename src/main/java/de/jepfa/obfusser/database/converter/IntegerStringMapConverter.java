package de.jepfa.obfusser.database.converter;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import de.jepfa.obfusser.util.encrypt.DbCrypt;

public class IntegerStringMapConverter {

    @TypeConverter
    public static Map<Integer, String> restoreMap(String listOfString) {
        String string;
        if ("{}".equals(listOfString)) {
            string = listOfString;
        }
        else {
            string = DbCrypt.INSTANCE.aesDecrypt(listOfString);
        }
        return new Gson().fromJson(
                string,
                new TypeToken<Map<Integer, String>>() {
                }.getType());
    }

    @TypeConverter
    public static String saveMap(Map<Integer, String> listOfString) {
        String serialized = new Gson().toJson(listOfString);
        if ("{}".equals(serialized)) {
            return serialized;
        }
        else {
            return DbCrypt.INSTANCE.aesEncrypt(serialized);
        }
    }
}
