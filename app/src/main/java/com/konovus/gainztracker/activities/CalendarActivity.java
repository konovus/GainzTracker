package com.konovus.gainztracker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.PopupWindow;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.adapters.WorkoutAdapter;
import com.konovus.gainztracker.database.WorkoutDatabase;
import com.konovus.gainztracker.databinding.ActivityCalendarBinding;
import com.konovus.gainztracker.models.Workout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.konovus.gainztracker.activities.MainActivity.path;

public class CalendarActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutListener{

    private static final int REQUEST_CODE_ADD_WORKOUT = 1;
    ActivityCalendarBinding binding;
    WorkoutAdapter adapter;
    List<Workout> workouts = new ArrayList<>();
    ViewStub viewStub;
    CalendarView calendar;
    List<CalendarDay> calendarDays;
    public static boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar);

        viewStub = findViewById(R.id.stub_calendar);

        Glide.with(getApplicationContext()).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(path + "/profile_img.jpg").into(binding.profileImg);

        binding.addWorkout.setOnClickListener(v -> startActivityForResult(
                new Intent(this, AddWorkout.class), REQUEST_CODE_ADD_WORKOUT));


        binding.dashboardBtn.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

//        binding.calendarContainer.post(() -> calendarSetup());
        new CalendarSetupAsync().execute();
        getWorkouts();
//        recyclerViewSetup();
//        calendarSetup();
        reportFullyDrawn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_WORKOUT)
            getWorkouts();
    }

    private void calendarSetup(){

        List<CalendarDay> calendarDays = new ArrayList<>();
        if(workouts != null)
        for(Workout workout : workouts){
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(workout.getDate()));
                CalendarDay calendarDay = new CalendarDay(cal);
                calendarDay.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_selected_day_cal_gradient));
                calendarDays.add(calendarDay);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(viewStub != null && !binding.stubCalendar.isInflated())
            calendar = (CalendarView) viewStub.inflate();

        calendar.setCalendarDays(calendarDays);
        calendar.setForwardButtonImage(ContextCompat.getDrawable(this, R.drawable.ic_arrow_right));
        calendar.setPreviousButtonImage(ContextCompat.getDrawable(this, R.drawable.ic_arrow_left));

        calendar.setOnForwardPageChangeListener(() -> {
            adapter.setWorkouts(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)));
            adapter.notifyDataSetChanged();
        });
        calendar.setOnPreviousPageChangeListener(() -> {
            adapter.setWorkouts(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)));
            adapter.notifyDataSetChanged();
        });
        calendar.setVisibility(View.VISIBLE);
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

                    new CalendarSetupAsync().execute();
                    recyclerViewSetup();

                    compositeDisposable.dispose();
                }));

    }

    private void recyclerViewSetup(){
        if(workouts == null)
            workouts = new ArrayList<>();
        adapter = new WorkoutAdapter(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)), this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void OnWorkoutClick(int pos, Workout workout, View view) {
        PopupWindow mypopupWindow;
            LayoutInflater inflater = (LayoutInflater)
                    getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popup_view = inflater.inflate(R.layout.popup_window_layout, null);

            mypopupWindow = new PopupWindow(popup_view,ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
            mypopupWindow.showAsDropDown(view);

            popup_view.findViewById(R.id.edit).setOnClickListener(v -> {
                Intent intent = new Intent(this, AddWorkout.class);
                intent.putExtra("workout", workout);
                startActivity(intent);
                mypopupWindow.dismiss();

            });
            popup_view.findViewById(R.id.delete).setOnClickListener(v -> {
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(WorkoutDatabase.getDatabase(getApplicationContext())
                        .workoutDao().deleteWorkout(workout)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            workouts.remove(workout);
                            adapter.setWorkouts(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)));
                            adapter.notifyItemRemoved(pos);

                            mypopupWindow.dismiss();
                            compositeDisposable.dispose();
                        }));
            });
    }

    private List<Workout> getFilteredWorkouts(int pos){
        List<Workout> data = new ArrayList<>();
        for(Workout workout: workouts)
            if((Integer.parseInt(workout.getDate().substring(3, 5)) < 10 ? workout.getDate().substring(4, 5) : workout.getDate().substring(3, 5))
                    .equals(String.valueOf(pos + 1)))
                data.add(workout);
        return data;
    }

    private class CalendarSetupAsync extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            calendarSetup();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(viewStub != null && !binding.stubCalendar.isInflated())
                calendar = (CalendarView) viewStub.inflate();

            calendar.setCalendarDays(calendarDays);
            calendar.setVisibility(View.VISIBLE);

            calendar.setForwardButtonImage(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_right));
            calendar.setPreviousButtonImage(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_left));

            calendar.setOnForwardPageChangeListener(() -> {
                adapter.setWorkouts(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)));
                adapter.notifyDataSetChanged();
            });
            calendar.setOnPreviousPageChangeListener(() -> {
                adapter.setWorkouts(getFilteredWorkouts(calendar.getCurrentPageDate().get(Calendar.MONTH)));
                adapter.notifyDataSetChanged();
            });
            super.onPostExecute(aVoid);
        }

        private void calendarSetup(){

            calendarDays = new ArrayList<>();
            if(workouts != null)
                for(Workout workout : workouts){
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(workout.getDate()));
                        CalendarDay calendarDay = new CalendarDay(cal);
                        calendarDay.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_selected_day_cal_gradient));
                        calendarDays.add(calendarDay);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


        }
    }

}