package com.konovus.gainztracker.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.ExercisesNamesAdapter;
import com.konovus.gainztracker.adapters.SetsAdapter;
import com.konovus.gainztracker.databinding.ActivityAddExerciseBinding;
import com.konovus.gainztracker.databinding.LayoutDeleteExerciseNameBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Set_exercise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

public class AddExercise extends AppCompatActivity implements ExercisesNamesAdapter.ExercisesNamesListener, SetsAdapter.SetsListener {

    private ActivityAddExerciseBinding binding;
    private ExercisesNamesAdapter adapter;
    private AlertDialog deleteDialog;
    private List<String> exercises_names;
    private SetsAdapter setsAdapter;
    private final Exercise exercise = new Exercise();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_exercise);

        binding.backArrow.setOnClickListener(v -> onBackPressed());


        spinnerSetup();
        recyclerViewSetup();
        inputExerciseNameSetup();
        setsNumberInputSetup();
        saveExerciseSetup();

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

    private void setsNumberInputSetup(){
        List<Set_exercise> setExercises = new ArrayList<>();
        setExercises.add(new Set_exercise());
        setsAdapter = new SetsAdapter(setExercises, this, this);
        binding.setsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.setsRecyclerView.setAdapter(setsAdapter);

        binding.leftArrow.setOnClickListener(v ->{
            if(setExercises.size() >= 1) {
                setsAdapter.removeItem(setExercises.size() - 1);
                setExercises.remove(setExercises.size() - 1);
                setsAdapter.setItems(setExercises);
                binding.setsNumber.setText(String.valueOf(setExercises.size()));
            }
        });

        binding.rightArrow.setOnClickListener(v ->{
            setsAdapter.addItem();
            setExercises.add(new Set_exercise());
            setsAdapter.setItems(setExercises);
            binding.setsNumber.setText(String.valueOf(setExercises.size()));
        });

    }

    private void saveExerciseSetup(){
        binding.saveBtn.setOnClickListener(v -> {
            List<Set_exercise> data = setsAdapter.getSetExercises();
            if(data != null && !data.isEmpty())
            for(Set_exercise setExercise : data){
                if (setExercise.getWeight() == null || setExercise.getWeight().toString().isEmpty())
                    setExercise.setWeight(0);
                if(setExercise.getReps() == 0) {
                    Toast.makeText(this, "Reps cannot be 0", Toast.LENGTH_LONG).show();
                    return;
                }
                if (exercise.getName() == null || exercise.getName().trim().isEmpty()) {
                    Toast.makeText(this, "Select the exercise name!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
                exercise.setMuscle(binding.spinnerMuscles.getSelectedItem().toString());
                exercise.setSetExercises(data);

                Intent intent = new Intent();
                intent.putExtra("exercise", exercise);
                setResult(RESULT_OK, intent);
                finish();

        });
    }

    private void spinnerSetup(){
        List<String> muscle_groups = new ArrayList<>();
        Collections.addAll(muscle_groups, "Chest", "Back", "Biceps", "Triceps", "Abs", "Legs", "Calves");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.muscle_spinner_row, muscle_groups);

        // attaching data adapter to spinner
        binding.spinnerMuscles.setAdapter(dataAdapter);

    }



    @Override
    public void OnExerciseNameClicked(int pos) {
        exercise.setName(exercises_names.get(pos));
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

    @Override
    public void OnSetsClick(int pos) {

    }
}