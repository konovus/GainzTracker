package com.konovus.gainztracker.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.WorkoutHAdapter;
import com.konovus.gainztracker.adapters.WorkoutVAdapter;
import com.konovus.gainztracker.database.WorkoutDatabase;
import com.konovus.gainztracker.databinding.ActivityMainBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Workout;
import com.konovus.gainztracker.utils.StorageUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements WorkoutHAdapter.WorkoutHListener {

    private ActivityMainBinding binding;

    public static final int REQUEST_CODE_ADD_WORKOUT = 1;
    public static final int REQUEST_CODE_EDIT_WORKOUT = 3;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;

    private AlertDialog dialog;
    private ImageView profile_image;
    private List<Workout> workouts;
    private final List<List<Exercise>> exercisesLists = new ArrayList<>();
    private WorkoutHAdapter hAdapter;
    private WorkoutVAdapter vAdapter;
    private String current_month;
    public Bitmap bitmap;
    private static String name;
    public static String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.nanoTime();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        long endTime_one = System.nanoTime();
        Log.i("TIME Measure", "After set content view - " + TimeUnit.NANOSECONDS.toMillis(endTime_one - startTime) + " - " + (endTime_one - startTime));
        binding.addWorkout.setOnClickListener(v -> startActivityForResult(
                new Intent(this, AddWorkout.class), REQUEST_CODE_ADD_WORKOUT));

        binding.calendarBtn.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));

        binding.profileImg.setOnClickListener(v -> profileDialogSetup());
        long endTime_two = System.nanoTime();
        Log.i("TIME Measure", "After simple methods - " + TimeUnit.NANOSECONDS.toMillis(endTime_two - endTime_one) + " - " + (endTime_two - endTime_one));

        if(path != null && name != null){
            binding.nameTv.setText(name);
            Glide.with(this).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .load(path + "/profile_img.jpg").into(binding.profileImg);
        } else new LoadInternallyAsync(binding.nameTv, binding.profileImg).execute();
        long endTime_three = System.nanoTime();
        Log.i("TIME Measure", "After profile img - " + TimeUnit.NANOSECONDS.toMillis(endTime_three - endTime_two) + " - " + (endTime_three - endTime_two));

        getWorkouts();
        long endTime_four = System.nanoTime();
        Log.i("TIME Measure", "After getWorkouts - " + TimeUnit.NANOSECONDS.toMillis(endTime_four - endTime_three) + " - " + (endTime_four - endTime_three));
        hRecyclerViewSetup();
        long endTime_five = System.nanoTime();
        Log.i("TIME Measure", "After Hrecycler - " + TimeUnit.NANOSECONDS.toMillis(endTime_five - endTime_four) + " - " + (endTime_five - endTime_four));
        vRecyclerViewSetup();
        long endTime_six = System.nanoTime();
        Log.i("TIME Measure", "After Vrecycler - " + TimeUnit.NANOSECONDS.toMillis(endTime_six - endTime_five) + " - " + (endTime_six - endTime_five));
        spinnerSetup();
        long endTime_seven = System.nanoTime();
        Log.i("TIME Measure", "After spinner - " + TimeUnit.NANOSECONDS.toMillis(endTime_seven - endTime_six) + " - " + (endTime_seven - endTime_six));

    }


    private void getWorkouts(){
        workouts = new ArrayList<>();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(WorkoutDatabase.getDatabase(getApplicationContext()).workoutDao().getAllWorkouts()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(workouts_current -> {
                    workouts.addAll(workouts_current);
                    workouts.sort(Collections.reverseOrder());
                        vAdapter.setExercises(getExercisesLists(Calendar.getInstance().get(Calendar.MONTH)),
                                binding.spinnerMonths.getSelectedItem().toString());
                        vAdapter.notifyDataSetChanged();
                        hAdapter.setWorkouts(getFilteredWorkouts(Calendar.getInstance().get(Calendar.MONTH)));
                        hAdapter.notifyDataSetChanged();
                    compositeDisposable.dispose();
                }));

    }

    private void hRecyclerViewSetup(){
        hAdapter = new WorkoutHAdapter(workouts, this, this);
        binding.recyclerViewHWorkouts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewHWorkouts.setAdapter(hAdapter);
        binding.recyclerViewHWorkouts.setVisibility(View.VISIBLE);
    }

    private void vRecyclerViewSetup(){
        vAdapter = new WorkoutVAdapter(exercisesLists, current_month,this);
        binding.recyclerViewVWorkouts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewVWorkouts.setAdapter(vAdapter);
        binding.recyclerViewVWorkouts.setVisibility(View.VISIBLE);
    }

    private void spinnerSetup(){
        List<String> months = new ArrayList<>();
        Collections.addAll( months, "January", "February", "March", "April", "May", "June", "July"
                , "August", "September", "October", "November", "December");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.muscle_spinner_row, months);

        // attaching data adapter to spinner
        binding.spinnerMonths.setAdapter(dataAdapter);
        binding.spinnerMonths.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        current_month = months.get(Calendar.getInstance().get(Calendar.MONTH) - 1);

        binding.spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vAdapter.setExercises(getExercisesLists(position), binding.spinnerMonths.getSelectedItem().toString());
                hAdapter.setWorkouts(getFilteredWorkouts(position));
                new Handler(Looper.getMainLooper()).post(() -> {
                    vAdapter.notifyDataSetChanged();
                    hAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void profileDialogSetup(){
        if(dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.profile_img_layout,
                    findViewById(R.id.profile_layout_container));
            builder.setView(view);

            dialog = builder.create();

            if(dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            profile_image = view.findViewById(R.id.profile_img);

            if(path == null)
                new LoadInternallyAsync((view.findViewById(R.id.name_et)), profile_image).execute();
            else {
                if(!name.equals("New_user"))
                    ((EditText)(view.findViewById(R.id.name_et))).setText(name);
                Glide.with(getApplicationContext()).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .load(path + "/profile_img.jpg").into(profile_image);
            }

            profile_image.setOnClickListener(v1 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else selectImage();
            });

            view.findViewById(R.id.ok_btn).setOnClickListener(v1 -> {
                if(bitmap != null || path != null) {
                    if(!((EditText)view.findViewById(R.id.name_et)).getText().toString().isEmpty()) {
                        name = ((EditText) view.findViewById(R.id.name_et)).getText().toString();

                        if(bitmap != null)
                            binding.profileImg.setImageBitmap(bitmap);
                        else
                            Glide.with(this).asBitmap()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .load(path + "/profile_img.jpg").into(binding.profileImg);

                        binding.nameTv.setText(name);

                        dialog.dismiss();
                        new SaveInternallyAsync().execute();

                    } else {
                        Toast.makeText(this, "Type your name first!", Toast.LENGTH_LONG).show();
                        return;
                    }

                } else {
                    Toast.makeText(this, "Select an image first!", Toast.LENGTH_LONG).show();
                    return;
                }

            });

            view.findViewById(R.id.cancel_btn).setOnClickListener(v1 -> dialog.dismiss());
        }
        dialog.show();
    }

    private void selectImage(){
        CropImage.activity()
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(MainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectImage();
            else Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = result.getUri();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        profile_image.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if(requestCode == REQUEST_CODE_ADD_WORKOUT || requestCode == REQUEST_CODE_EDIT_WORKOUT)
            getWorkouts();
    }

    @Override
    public void OnWorkoutClick(int pos, View view, Workout workout) {
        PopupWindow mypopupWindow;
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popup_view = inflater.inflate(R.layout.popup_window_layout, null);

        mypopupWindow = new PopupWindow(popup_view, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
        mypopupWindow.showAsDropDown(view);

        popup_view.findViewById(R.id.edit).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWorkout.class);
            intent.putExtra("workout", getFilteredWorkouts(binding.spinnerMonths.getSelectedItemPosition()).get(pos));
            startActivityForResult(intent, REQUEST_CODE_EDIT_WORKOUT);
            mypopupWindow.dismiss();

        });
        popup_view.findViewById(R.id.delete).setOnClickListener(v -> {
            CompositeDisposable compositeDisposable = new CompositeDisposable();
            compositeDisposable.add(WorkoutDatabase.getDatabase(getApplicationContext())
                    .workoutDao().deleteWorkout(workouts.get(pos))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        workouts.remove(workout);
                        hAdapter.setWorkouts(getFilteredWorkouts(binding.spinnerMonths.getSelectedItemPosition()));
                        hAdapter.notifyItemRemoved(pos);
                        mypopupWindow.dismiss();
                        compositeDisposable.dispose();
                    }));
        });
    }

    private List<List<Exercise>> getExercisesLists(int month){
        List<List<Exercise>> exercisesLists = new ArrayList<>();
        List<Workout> data = getFilteredWorkouts(month);
        List<Exercise> exercises = new ArrayList<>();
        for(Workout workout : data) {
            for(Exercise exercise : workout.getExercises())
                if(exercise.getDate() == null || exercise.getDate().isEmpty())
                    exercise.setDate(workout.getDate());
            exercises.addAll(workout.getExercises());
        }

        NavigableMap<String, List<Exercise>> exercisesByMuscle = new TreeMap<>();
        for (Exercise exercise : exercises) {
            List<Exercise> exerciseList = exercisesByMuscle.get(exercise.getMuscle());
            if (exerciseList == null)
                exercisesByMuscle.put(exercise.getMuscle(), exerciseList = new ArrayList<>());

            exerciseList.add(exercise);
        }

        for(String key : exercisesByMuscle.keySet())
            exercisesLists.add(exercisesByMuscle.get(key));

        return exercisesLists;
    }

    private List<Workout> getFilteredWorkouts(int pos){
        List<Workout> data = new ArrayList<>();
        for(Workout workout: workouts)
            if((Integer.parseInt(workout.getDate().substring(3, 5)) < 10 ? workout.getDate().substring(4, 5) : workout.getDate().substring(3, 5))
                    .equals(String.valueOf(pos+ 1)))
                data.add(workout);
        return data;
    }

    private class SaveInternallyAsync extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String path = StorageUtils.saveToInternalStorage(bitmap, MainActivity.this, "profile_img");
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("profile_img_path", path).apply();

            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("username",
                    (name)).apply();
            return null;
        }
    }
    private class LoadInternallyAsync extends AsyncTask<Void, Void, Void>{

        private TextView textView;
        private ImageView imageView;

        public LoadInternallyAsync(TextView textView, ImageView imageView) {
            this.textView = textView;
            this.imageView = imageView;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString("username",  "New_user");

            path = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("profile_img_path", "");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!name.equals("New_user"))
                textView.setText(name);
            if(!path.isEmpty())
                Glide.with(imageView.getContext()).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .load(path + "/profile_img.jpg").into(imageView);

            super.onPostExecute(aVoid);
        }
    }
}