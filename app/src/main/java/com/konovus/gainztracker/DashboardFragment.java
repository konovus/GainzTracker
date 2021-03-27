package com.konovus.gainztracker;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.konovus.gainztracker.activities.AddWorkout;
import com.konovus.gainztracker.adapters.WorkoutHAdapter;
import com.konovus.gainztracker.adapters.WorkoutVAdapter;
import com.konovus.gainztracker.database.WorkoutDatabase;
import com.konovus.gainztracker.databinding.FragmentDashboardBinding;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.konovus.gainztracker.activities.MainActivity.path;

public class DashboardFragment extends Fragment implements WorkoutHAdapter.WorkoutHListener{

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
    FragmentDashboardBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false);
        View view = binding.getRoot();

        binding.profileImg.setOnClickListener(v -> profileDialogSetup());

        if(path != null && name != null){
            binding.nameTv.setText(name);
            Glide.with(this).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .load(path + "/profile_img.jpg").into(binding.profileImg);
        } else new LoadInternallyAsync(binding.nameTv, binding.profileImg).execute();

        getWorkouts();
        hRecyclerViewSetup();
        vRecyclerViewSetup();
        spinnerSetup();

        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(new_workout_d)
//            getWorkouts();
//        new_workout_d = false;
//        new_workout_c = false;
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectImage();
            else Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageUri = result.getUri();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        profile_image.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
//        if(requestCode == REQUEST_CODE_ADD_WORKOUT || requestCode == REQUEST_CODE_EDIT_WORKOUT)
//            getWorkouts();
    }

    private void getWorkouts(){
        workouts = new ArrayList<>();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(WorkoutDatabase.getDatabase(getActivity()).workoutDao().getAllWorkouts()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(workouts_current -> {
                    workouts.clear();
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
        hAdapter = new WorkoutHAdapter(workouts, getActivity(), this);
        binding.recyclerViewHWorkouts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewHWorkouts.setAdapter(hAdapter);
    }

    private void vRecyclerViewSetup(){
        vAdapter = new WorkoutVAdapter(exercisesLists, current_month,getContext());
        binding.recyclerViewVWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewVWorkouts.setAdapter(vAdapter);
    }

    private void spinnerSetup(){
        List<String> months = new ArrayList<>();
        Collections.addAll( months, "January", "February", "March", "April", "May", "June", "July"
                , "August", "September", "October", "November", "December");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.muscle_spinner_row, months);

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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.profile_img_layout,
                    getActivity().findViewById(R.id.profile_layout_container));
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
                Glide.with(getContext()).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .load(path + "/profile_img.jpg").into(profile_image);
            }

            profile_image.setOnClickListener(v1 -> {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
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
                        Toast.makeText(getActivity(), "Type your name first!", Toast.LENGTH_LONG).show();
                        return;
                    }

                } else {
                    Toast.makeText(getActivity(), "Select an image first!", Toast.LENGTH_LONG).show();
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
                .start(getActivity());
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

    @Override
    public void OnWorkoutClick(int pos, View view, Workout workout) {
        PopupWindow mypopupWindow;
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popup_view = inflater.inflate(R.layout.popup_window_layout, null);

        mypopupWindow = new PopupWindow(popup_view, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
        mypopupWindow.showAsDropDown(view);

        popup_view.findViewById(R.id.edit).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddWorkout.class);
            intent.putExtra("workout", getFilteredWorkouts(binding.spinnerMonths.getSelectedItemPosition()).get(pos));
            startActivityForResult(intent, REQUEST_CODE_EDIT_WORKOUT);
            mypopupWindow.dismiss();

        });
        popup_view.findViewById(R.id.delete).setOnClickListener(v -> {
            CompositeDisposable compositeDisposable = new CompositeDisposable();
            compositeDisposable.add(WorkoutDatabase.getDatabase(getContext())
                    .workoutDao().deleteWorkout(workouts.get(pos))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        workouts.remove(workout);
                        hAdapter.setWorkouts(getFilteredWorkouts(binding.spinnerMonths.getSelectedItemPosition()));
                        hAdapter.notifyItemRemoved(pos);
                        vAdapter.setExercises(getExercisesLists(binding.spinnerMonths.getSelectedItemPosition()),
                                binding.spinnerMonths.getSelectedItem().toString());
                        vAdapter.notifyDataSetChanged();
                        mypopupWindow.dismiss();
                        compositeDisposable.dispose();
                    }));
        });
    }

    private class SaveInternallyAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String path = StorageUtils.saveToInternalStorage(bitmap, getContext(), "profile_img");
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("profile_img_path", path).apply();

            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("username",
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
            name = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString("username",  "New_user");

            path = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("profile_img_path", "");

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