package com.konovus.gainztracker.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.ActivityMainBinding;

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