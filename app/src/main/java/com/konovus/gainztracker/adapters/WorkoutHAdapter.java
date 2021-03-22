package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.WorkoutHLayoutItemBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Workout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class WorkoutHAdapter extends RecyclerView.Adapter<WorkoutHAdapter.WorkoutHViewHolder> {

    private List<Workout> workouts;
    private Context context;
    private LayoutInflater layoutInflater;
    private WorkoutHAdapter.WorkoutHListener workoutHListener;

    public WorkoutHAdapter(List<Workout> workouts, Context context, WorkoutHListener workoutHListener) {
        this.workouts = workouts;
        this.context = context;
        this.workoutHListener = workoutHListener;
    }

    @NonNull
    @Override
    public WorkoutHViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        WorkoutHLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.workout_h_layout_item, parent, false
        );
        return new WorkoutHViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutHViewHolder holder, int position) {
        holder.setBinding(workouts.get(position));
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public interface WorkoutHListener{
        void OnWorkoutClick(int pos, View view, Workout workout);
    }

    public void setWorkouts(List<Workout> workouts){
        this.workouts = workouts;
    }


    public class WorkoutHViewHolder extends RecyclerView.ViewHolder{

        private WorkoutHLayoutItemBinding binding;

        public WorkoutHViewHolder(WorkoutHLayoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(Workout workout){
            StringBuilder muscle = new StringBuilder();
            for (Exercise exercise : workout.getExercises())
                if(!muscle.toString().contains(exercise.getMuscle()))
                        muscle.append(exercise.getMuscle()).append(" ");

            binding.muscleName.setText(muscle.toString().trim().replaceAll(" ", " & "));

            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("EEE, MMM d");
            Date date;
            try {
                date = format1.parse(workout.getDate());
                binding.date.setText(format2.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            binding.showPopupMenu.setOnClickListener(v -> workoutHListener.OnWorkoutClick(getAdapterPosition(), v, workout));
        }
    }
}
