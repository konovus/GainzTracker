package com.konovus.gainztracker.type_converters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.konovus.gainztracker.models.Exercise;

import java.lang.reflect.Type;
import java.util.List;

import androidx.room.TypeConverter;

public class ExercisesConverter {

    @TypeConverter
    public String fromExercises(List<Exercise> exercises){
        if(exercises == null)
            return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<Exercise>>() {}.getType();
        String json = gson.toJson(exercises, type);
        return json;
    }
    @TypeConverter
    public List<Exercise> toExercises(String exercises) {
        if (exercises == null)
            return null;

        Gson gson = new Gson();
        Type type = new TypeToken<List<Exercise>>() {}.getType();
        return gson.fromJson(exercises, type);
    }
}
