package com.konovus.gainztracker.database;

import android.content.Context;

import com.konovus.gainztracker.dao.WorkoutDao;
import com.konovus.gainztracker.models.Workout;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Workout.class, version = 1, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    private static WorkoutDatabase workoutDatabase;

    public static synchronized WorkoutDatabase getDatabase(Context context){
        if(workoutDatabase == null){
            workoutDatabase = Room.databaseBuilder(
                    context, WorkoutDatabase.class, "workouts_db"
            ).build();
        }

        return workoutDatabase;
    }

    public abstract WorkoutDao workoutDao();
}
