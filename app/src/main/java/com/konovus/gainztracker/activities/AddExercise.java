package com.konovus.gainztracker.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.ExercisesNamesAdapter;
import com.konovus.gainztracker.databinding.ActivityAddExerciseBinding;
import com.konovus.gainztracker.databinding.LayoutDeleteExerciseNameBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddExercise extends AppCompatActivity implements ExercisesNamesAdapter.ExercisesNamesListener {

    private ActivityAddExerciseBinding binding;
    private ExercisesNamesAdapter adapter;
    private AlertDialog deleteDialog;
    List<String> exercises_names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_exercise);

        inputExerciseNameSetup();
        recyclerViewSetup();
        spinnerSetup();
    }

    private void inputExerciseNameSetup(){
        binding.exerciseNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().trim().isEmpty())
                    binding.addExerciseNameBtn.setVisibility(View.VISIBLE);
                else binding.addExerciseNameBtn.setVisibility(View.GONE);
            }
        });
        binding.addExerciseNameBtn.setOnClickListener(v -> {
            adapter.addItem(binding.exerciseNameInput.getText().toString());
            binding.exerciseNameInput.setText("");
            binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void recyclerViewSetup(){
        exercises_names = new ArrayList<>();
        Collections.addAll(exercises_names, "Suspended inverted row", "Deadlift", "Back squat", "Bench Press",
                "Dumbbell romanian deadlift", "Pullup", "Barbell overhead press", "Barbell hip thrust");
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        binding.exercisesNamesRecyclerView.setLayoutManager(layoutManager);
        binding.exercisesNamesRecyclerView.setNestedScrollingEnabled(false);
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
    protected void onResume() {
        super.onResume();
        if(getIntent().getBooleanExtra("isExerciseNameDeleted", false)){
            exercises_names.remove(getIntent().getIntExtra("pos", -1));
            adapter.setNames(exercises_names);
            adapter.notifyItemRemoved(getIntent().getIntExtra("pos", -1));
        }
    }


    @Override
    public void OnExerciseNameClicked(int pos, View view) {

    }

    @Override
    public void OnExerciseNameLongClicked(int pos, View view) {
        showDeleteNoteDialog(pos);
    }

    private void showDeleteNoteDialog(int pos){
        if(deleteDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_exercise_name,
                    findViewById(R.id.layoutDeleteNoteContainer));
            builder.setView(view);
            LayoutDeleteExerciseNameBinding deleteExerciseNameBinding = DataBindingUtil.bind(view);

            deleteDialog = builder.create();
            if(deleteDialog.getWindow() != null)
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            deleteExerciseNameBinding.deleteBTN.setOnClickListener(v -> {
                adapter.removeItem(pos);
                deleteDialog.dismiss();
            });
            deleteExerciseNameBinding.cancelDelete.setOnClickListener(v -> deleteDialog.dismiss());
        }
        deleteDialog.show();
    }
}