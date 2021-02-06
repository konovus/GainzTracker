package com.konovus.gainztracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;

import com.konovus.gainztracker.databinding.ActivityAddWorkoutBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddWorkout extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_EXERCISE = 1;
    ActivityAddWorkoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_workout);

        binding.dateInput.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        final Shader textShader=new LinearGradient(0, 0, 0, 100,
                new int[]{Color.parseColor("#82F6DB"),Color.parseColor("#009FAE")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        binding.addNewExerciseBtn.getPaint().setShader(textShader);

        binding.backArrow.setOnClickListener(v -> onBackPressed());

        binding.addNewExerciseBtn.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AddExercise.class), REQUEST_CODE_ADD_EXERCISE));

    }
}