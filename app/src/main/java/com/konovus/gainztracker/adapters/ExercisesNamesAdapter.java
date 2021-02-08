package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.FlexboxEcercisesNamesItemBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ExercisesNamesAdapter extends RecyclerView.Adapter<ExercisesNamesAdapter.ExercisesNamesViewHolder> {

    private List<String> names;
    private LayoutInflater layoutInflater;
    private final ExercisesNamesListener listener;
    Context context;
    private int active_pos = -1;

    public ExercisesNamesAdapter(List<String> names, ExercisesNamesListener listener, Context context) {
        this.names = names;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ExercisesNamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        FlexboxEcercisesNamesItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.flexbox_ecercises_names_item, parent, false
        );
        return new ExercisesNamesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExercisesNamesViewHolder holder, int position) {
        holder.bindExerciseName(names.get(position), position);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public interface ExercisesNamesListener{
        void OnExerciseNameClicked(int pos, View view);
        void OnExerciseNameLongClicked(int pos, View view);
    }

    public class ExercisesNamesViewHolder extends RecyclerView.ViewHolder{
         private FlexboxEcercisesNamesItemBinding binding;

        public ExercisesNamesViewHolder( FlexboxEcercisesNamesItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindExerciseName(String name, int pos){
            binding.setName(name);
            if(active_pos == pos){
                binding.exerciseName.setBackgroundResource(R.drawable.bg_exercises_names_active);
                binding.exerciseName.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
            } else {
                binding.exerciseName.setBackgroundResource(R.drawable.bg_exercises_names);
                binding.exerciseName.setTextColor(ContextCompat.getColor(context, R.color.blue));
            }

            binding.executePendingBindings();
            binding.getRoot().setOnClickListener(v -> {
                binding.exerciseName.setBackgroundResource(R.drawable.bg_exercises_names_active);
                binding.exerciseName.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
                if(active_pos != getAdapterPosition() && active_pos != -1)
                    notifyItemChanged(active_pos);
                active_pos = getAdapterPosition();
                listener.OnExerciseNameClicked(active_pos, v);
            });
            binding.getRoot().setOnLongClickListener(v -> {
                listener.OnExerciseNameLongClicked(getAdapterPosition(), v);
                return true;
            });
        }
    }

    public void setNames(List<String> current_names){
        names = current_names;
    }

    public void removeItem(int pos){
        names.remove(pos);
        notifyItemRemoved(pos);
    }
    public void addItem(String name){
        names.add(name);
        notifyItemInserted(names.size()-1);
    }
}
