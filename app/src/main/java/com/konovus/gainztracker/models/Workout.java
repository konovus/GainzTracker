package com.konovus.gainztracker.models;

import com.konovus.gainztracker.type_converters.ExercisesConverter;

import java.io.Serializable;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "workouts")
public class Workout implements Serializable, Comparable<Workout> {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date;

    @TypeConverters(ExercisesConverter.class)
    private List<Exercise> exercises;

    public Workout(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @Override
    public int compareTo(Workout o) {
        return getDate().compareTo(o.getDate());
    }
}
