package com.konovus.gainztracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.konovus.gainztracker.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public static final int REQUEST_CODE_ADD_WORKOUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.addWorkout.setOnClickListener(v -> startActivityForResult(
                new Intent(this, AddWorkout.class), REQUEST_CODE_ADD_WORKOUT));


    }


}