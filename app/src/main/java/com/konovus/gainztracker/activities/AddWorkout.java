package com.konovus.gainztracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.ExercisesAdapter;
import com.konovus.gainztracker.database.WorkoutDatabase;
import com.konovus.gainztracker.databinding.ActivityAddWorkoutBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddWorkout extends AppCompatActivity implements ExercisesAdapter.ExercisesListener {

    public static final int REQUEST_CODE_ADD_EXERCISE = 1;
    ActivityAddWorkoutBinding binding;
    List<Exercise> exercises;
    ExercisesAdapter adapter;
    private Workout workout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_workout);

        workout = (Workout) getIntent().getSerializableExtra("workout");

        binding.dateInput.setText(workout != null ? workout.getDate() :
                new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(new Date()));

        final Shader textShader=new LinearGradient(0, 0, 0, 100,
                new int[]{Color.parseColor("#82F6DB"),Color.parseColor("#009FAE")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        binding.addNewExerciseBtn.getPaint().setShader(textShader);

        binding.backArrow.setOnClickListener(v -> onBackPressed());

        binding.addNewExerciseBtn.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AddExercise.class), REQUEST_CODE_ADD_EXERCISE));

        binding.saveBtn.setOnClickListener(v -> saveWorkout());

        recyclerViewSetup();

    }

    private void saveWorkout() {
        if(workout == null)
            workout = new Workout();
        
        if(binding.dateInput.getText().toString().trim().matches("\\d{2}[./]\\d{2}[./]\\d{4}"))
            workout.setDate(binding.dateInput.getText().toString());
        else {
            Toast.makeText(this, "Date format is wrong, \"DD/MM/YYYY\" !", Toast.LENGTH_LONG).show();
            return;
        }
        if(!exercises.isEmpty()) {
            for(Exercise exercise : exercises)
                exercise.setDate(binding.dateInput.getText().toString());
            workout.setExercises(exercises);
        }
        else {
            Toast.makeText(this, "Add an exercise!", Toast.LENGTH_LONG).show();
            return;
        }

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(WorkoutDatabase.getDatabase(getApplicationContext()).workoutDao().insertWorkout(workout)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    onBackPressed();
                    compositeDisposable.dispose();
                }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void recyclerViewSetup(){
        if(workout != null)
            exercises = workout.getExercises();
        else exercises = new ArrayList<>();

        adapter = new ExercisesAdapter(exercises, this, this);
        binding.exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.exercisesRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_ADD_EXERCISE && resultCode == RESULT_OK) {
            exercises.add((Exercise) data.getSerializableExtra("exercise"));
            if(workout != null)
                exercises.get(exercises.size() - 1).setDate(workout.getDate());
            adapter.addItem();
            adapter.setItems(exercises);
        }
    }

    @Override
    public void OnDeleteBtnClick(int pos) {
        exercises.remove(pos);
        adapter.notifyItemRemoved(pos);
    }
}