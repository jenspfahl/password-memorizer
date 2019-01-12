package de.jepfa.obfusser.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class IntegerStringMapConverter {

    @TypeConverter
    public static Map<Integer, String> restoreMap(String listOfString) {
        return new Gson().fromJson(
                listOfString,
                new TypeToken<Map<Integer, String>>() {}.getType());
    }

    @TypeConverter
    public static String saveMap(Map<Integer, String> listOfString) {
        return new Gson().toJson(listOfString);
    }
}
