package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.SetsLayoutItemBinding;
import com.konovus.gainztracker.models.Set_exercise;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetsViewHolder> {

    private List<Set_exercise> setExercises;
    private Context context;
    private LayoutInflater layoutInflater;
    private SetsListener setsListener;

    public SetsAdapter(List<Set_exercise> setExercises, Context context, SetsListener setsListener) {
        this.setExercises = setExercises;
        this.context = context;
        this.setsListener = setsListener;
    }

    @NonNull
    @Override
    public SetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        SetsLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.sets_layout_item, parent, false
        );
        return new SetsAdapter.SetsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SetsViewHolder holder, int position) {
        holder.bindSets(setExercises.get(position), position);
    }

    @Override
    public int getItemCount() {
        return setExercises.size();
    }

    public class SetsViewHolder extends RecyclerView.ViewHolder{

        private SetsLayoutItemBinding binding;

        public SetsViewHolder(SetsLayoutItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindSets(Set_exercise setExercise, int pos){
            binding.setSetNumber(setExercises.size());

            binding.repsInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().trim().isEmpty())
                        setExercise.setReps(Integer.parseInt(s.toString()));
                }
            });
            if(pos == setExercises.size() - 1)
                binding.weightInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
            else binding.weightInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            binding.weightInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().trim().isEmpty())
                        setExercise.setWeight(Integer.parseInt(s.toString().isEmpty() ? "0" : s.toString()));
                }
            });

        }
    }

    public interface SetsListener{
        void OnSetsClick(int pos);
    }

    public void setItems(List<Set_exercise> current_items){
        setExercises = current_items;
    }

    public List<Set_exercise> getSetExercises(){return setExercises;}

    public void removeItem(int pos){
// I'm using notifyDataSetChanged in order to change in all weight inputs ImeOptions
        notifyDataSetChanged();
//        notifyItemRemoved(pos);
    }
    public void addItem(){
        notifyDataSetChanged();
//        notifyItemInserted(setExercises.size());
    }
}
