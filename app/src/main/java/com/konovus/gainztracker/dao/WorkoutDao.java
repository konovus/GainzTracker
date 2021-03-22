package com.konovus.gainztracker.dao;

import com.konovus.gainztracker.models.Workout;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY id DESC")
    Flowable<List<Workout>> getAllWorkouts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWorkout(Workout workout);

    @Delete
    Completable deleteWorkout(Workout workout);

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    Flowable<Workout> getWorkoutById(String workoutId);
}
