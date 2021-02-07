package com.konovus.gainztracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.ExercisesNamesAdapter;
import com.konovus.gainztracker.databinding.ActivityAddExerciseBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddExercise extends AppCompatActivity implements ExercisesNamesAdapter.ExercisesNamesListener {

    private ActivityAddExerciseBinding binding;
    private ExercisesNamesAdapter adapter;
    int last_active = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_exercise);

        recyclerViewSetup();
        spinnerSetup();
    }

    private void recyclerViewSetup(){
        List<String> exercises_names = new ArrayList<>();
        Collections.addAll(exercises_names, "Suspended inverted row", "Deadlift", "Back squat", "Bench Press",
                "Dumbbell romanian deadlift", "Pullup", "Barbell overhead press", "Barbell hip thrust");
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        binding.exercisesNamesRecyclerView.setLayoutManager(layoutManager);
        adapter = new ExercisesNamesAdapter(exercises_names, this, this);
        binding.exercisesNamesRecyclerView.setAdapter(adapter);

    }

    private void spinnerSetup(){
        List<String> muscle_groups = new ArrayList<>();
        Collections.addAll(muscle_groups, "Chest", "Back", "Biceps", "Triceps", "Abs", "Legs", "Calves");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.muscle_spinner_row, muscle_groups);

        // attaching data adapter to spinner
        binding.spinnerMuscles.setAdapter(dataAdapter);
    }

    @Override
    public void OnExerciseNameClicked(int pos, View view) {

    }
}