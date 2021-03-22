package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.ExercisesLayoutItemBinding;
import com.konovus.gainztracker.databinding.SetsLayoutRowBinding;
import com.konovus.gainztracker.models.Exercise;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ExercisesViewHolder> {

    private List<Exercise> exercises;
    private Context context;
    private LayoutInflater layoutInflater;
    private ExercisesListener exercisesListener;

    public ExercisesAdapter(List<Exercise> exercises, Context context, ExercisesListener exercisesListener) {
        this.exercises = exercises;
        this.context = context;
        this.exercisesListener = exercisesListener;
    }

    @NonNull
    @Override
    public ExercisesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        ExercisesLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.exercises_layout_item, parent, false
        );
        return new ExercisesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExercisesViewHolder holder, int position) {
        holder.setBinding(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public interface ExercisesListener{
        void OnDeleteBtnClick(int pos);
    }

    public class ExercisesViewHolder extends RecyclerView.ViewHolder{

        ExercisesLayoutItemBinding binding;

        public ExercisesViewHolder(ExercisesLayoutItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(Exercise exercise){
            binding.setName(exercise.getName());
            if(exercise.getDate() != null)
                binding.deleteExercise.setVisibility(View.VISIBLE);
            else binding.deleteExercise.setVisibility(View.GONE);

            binding.deleteExercise.setOnClickListener(v -> exercisesListener.OnDeleteBtnClick(getAdapterPosition()));

            binding.setsListLayout.removeAllViews();

            for(int i = 0; i < exercise.getSetExercises().size(); i++){

                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.sets_layout_row, null);
                    SetsLayoutRowBinding setsLayoutRowBinding = DataBindingUtil.bind(linearLayout);
                    setsLayoutRowBinding.setSetNr(i + 1);
                    setsLayoutRowBinding.setRepsNr(exercise.getSetExercises().get(i).getReps());
                    setsLayoutRowBinding.setWeightNr(exercise.getSetExercises().get(i).getWeight());

                    binding.setsListLayout.addView(linearLayout);
            }
        }
    }

    public void setItems(List<Exercise> current_items){
        exercises = current_items;
    }


    public void removeItem(int pos){
        notifyItemRemoved(pos);
    }
    public void addItem(){
        notifyItemInserted(exercises.size());
    }
}
