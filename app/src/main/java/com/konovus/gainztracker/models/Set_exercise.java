package com.konovus.gainztracker.models;

import java.io.Serializable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sets")
public class Set_exercise implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int reps;
    private Integer weight;
    private int maxWeight;

    public Set_exercise(){}

    public Set_exercise(int id, int reps, Integer weight) {
        this.id = id;
        this.reps = reps;
        this.weight = weight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
