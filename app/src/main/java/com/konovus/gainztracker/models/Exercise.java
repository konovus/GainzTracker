package com.konovus.gainztracker.models;

import com.konovus.gainztracker.type_converters.SetsConverter;

import java.io.Serializable;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "exercises")
public class Exercise implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String muscle;
    private String date;
    @TypeConverters(SetsConverter.class)
    private List<Set_exercise> setExercises;

    public Exercise(){}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMuscle() {
        return muscle;
    }

    public void setMuscle(String muscle) {
        this.muscle = muscle;
    }

    public List<Set_exercise> getSetExercises() {
        return setExercises;
    }

    public void setSetExercises(List<Set_exercise> setExercises) {
        this.setExercises = setExercises;
    }
}
