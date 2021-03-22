package com.konovus.gainztracker.type_converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.konovus.gainztracker.models.Set_exercise;

import java.lang.reflect.Type;

import androidx.room.TypeConverter;

public class SetsConverter {

    @TypeConverter
    public String fromSet(Set_exercise setExercise){
        if(setExercise == null)
            return null;
        Gson gson = new Gson();
        Type type = new TypeToken<Set_exercise>() {}.getType();
        String json = gson.toJson(setExercise, type);
        return json;
    }
    @TypeConverter
    public Set_exercise toSet(String set) {
        if (set == null)
            return null;

        Gson gson = new Gson();
        Type type = new TypeToken<Set_exercise>() {}.getType();
        return gson.fromJson(set, type);
    }
}
