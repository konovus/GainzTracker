package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.ExercisesLayoutRowBinding;
import com.konovus.gainztracker.databinding.WorkoutLayoutItemBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Workout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class WorkoutAdapter  extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private Context context;
    private LayoutInflater layoutInflater;
    private WorkoutListener workoutListener;

    public WorkoutAdapter(List<Workout> workouts, Context context, WorkoutListener workoutListener) {
        this.workouts = workouts;
        this.context = context;
        this.workoutListener = workoutListener;

    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        WorkoutLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.workout_layout_item, parent, false
        );
        return new WorkoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.setBinding(workouts.get(position));
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public interface WorkoutListener{
        void OnWorkoutClick(int pos, Workout workout, View view);
    }

    public void setWorkouts(List<Workout> workouts){
        this.workouts = workouts;
    }

    public class WorkoutViewHolder extends RecyclerView.ViewHolder{

        WorkoutLayoutItemBinding binding;

        public WorkoutViewHolder(WorkoutLayoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(Workout workout) {
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("EEEE, MMMM d");
            try {
                Date date = format1.parse(workout.getDate());
                binding.date.setText(format2.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            binding.exercisesContainerLl.removeAllViews();


            NavigableMap<String, List<Exercise>> exercisesByMuscle = new TreeMap<>();
            for (Exercise exercise : workout.getExercises()) {
                List<Exercise> exerciseList = exercisesByMuscle.get(exercise.getMuscle());
                if (exerciseList == null)
                    exercisesByMuscle.put(exercise.getMuscle(), exerciseList = new ArrayList<>());

                exerciseList.add(exercise);
            }

            for(Map.Entry<String, List<Exercise>> map : exercisesByMuscle.entrySet()){

                ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context)
                        .inflate(R.layout.exercises_layout_row, null);
                ExercisesLayoutRowBinding exercisesBinding = DataBindingUtil.bind(constraintLayout);
                exercisesBinding.muscleName.setText(map.getKey());
                exercisesBinding.exercisesCount.setText(map.getValue().size() +
                        (map.getValue().size() > 1 ? " exercises" : " exercise"));

                binding.exercisesContainerLl.addView(constraintLayout);

            }

            binding.showPopupMenu.setOnClickListener(v -> workoutListener.OnWorkoutClick(getAdapterPosition(), workout, v));
        }
    }
}
