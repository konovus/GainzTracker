package com.konovus.gainztracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.konovus.gainztracker.databinding.ActivityAddExerciseBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddExercise extends AppCompatActivity {

    private ActivityAddExerciseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_exercise);

        spinnerSetup();
    }

    private void spinnerSetup(){
        List<String> muscle_groups = new ArrayList<>();
        Collections.addAll(muscle_groups, "Chest", "Back", "Biceps", "Triceps", "Abs", "Legs", "Calves");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.muscle_spinner_row, muscle_groups);

        // attaching data adapter to spinner
        binding.spinnerMuscles.setAdapter(dataAdapter);
    }
}