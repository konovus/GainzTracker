package com.konovus.gainztracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.konovus.gainztracker.R;
import com.konovus.gainztracker.databinding.WorkoutsVLayoutItemBinding;
import com.konovus.gainztracker.models.Exercise;
import com.konovus.gainztracker.models.Set_exercise;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class WorkoutVAdapter extends RecyclerView.Adapter<WorkoutVAdapter.WorkoutVViewHolder> {

    private List<List<Exercise>> exercises;
    private Context context;
    private LayoutInflater layoutInflater;
    private String month;

    public WorkoutVAdapter(List<List<Exercise>> exercises, String month, Context context) {
        this.exercises = exercises;
        this.context = context;
        this.month = month;
    }

    @NonNull
    @Override
    public WorkoutVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        WorkoutsVLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.workouts_v_layout_item, parent, false
        );
        return new WorkoutVViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutVViewHolder holder, int position) {
        holder.setBinding(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(List<List<Exercise>> exercises, String month){
        this.exercises = exercises;
        this.month = month;
    }


    public class WorkoutVViewHolder extends RecyclerView.ViewHolder{

        private WorkoutsVLayoutItemBinding binding;

        public WorkoutVViewHolder(WorkoutsVLayoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(List<Exercise> exercises){
            if(exercises != null || !exercises.isEmpty()){
                binding.muscleName.setText(exercises.get(0).getMuscle());
                binding.month.setText(month);

                Date date = null;
                try {
                    date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(month);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int iMonth = cal.get(Calendar.MONTH) + 1;
                final ArrayList<String> xLabel = new ArrayList<>();
                for(int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++)
                    xLabel.add(iMonth + "/" + i);

                final ArrayList<Entry> values_zero = new ArrayList();
                for(int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++)
                    values_zero.add(new Entry(i, 10));

                ArrayList<Entry> values = new ArrayList();
                for(Exercise exercise : exercises){
                    String d = "";
                    if(exercise.getDate().startsWith("0"))
                        d = exercise.getDate().substring(1,2);
                    else d = exercise.getDate().substring(0,2);
                    values.add(new Entry(Integer.parseInt(d),
                            exercise.getSetExercises().get(0).getWeight() == null ? 0 : getMaxWeight(exercise.getSetExercises())));
                }

                LineDataSet set1 = new LineDataSet(values, "");;
                set1.setValueTextColor(ContextCompat.getColor(context, R.color.white));
                set1.setValueTextSize(12f);

                LineDataSet set_zero = new LineDataSet(values_zero, "");;
                set_zero.setColor(ContextCompat.getColor(context, R.color.transparent));
                set_zero.setCircleColor(ContextCompat.getColor(context, R.color.transparent));
                set_zero.setCircleHoleColor(ContextCompat.getColor(context, R.color.transparent));
                set_zero.setDrawValues(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);
                dataSets.add(set_zero);
                LineData data = new LineData(dataSets);

                XAxis xAxis = binding.lineChart.getXAxis();
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        int index = (int) value;
                        if (index < 0 || index >= xLabel.size()) {
                            return "";
                        } else {
                            return xLabel.get(index);
                        }
                    }
                });
                xAxis.setAvoidFirstLastClipping(true);
                xAxis.setDrawAxisLine(false);
                xAxis.setDrawGridLines(false);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
                xAxis.setTextSize(12);
                xAxis.setYOffset(5);
                xAxis.setLabelCount(5,true);

                YAxis axisRight = binding.lineChart.getAxisRight();
                axisRight.setDrawGridLines(false);
                axisRight.setEnabled(false);
                YAxis axisLeft = binding.lineChart.getAxisLeft();
                axisLeft.setDrawAxisLine(false);
                axisLeft.setTextSize(11);
                axisLeft.setLabelCount(4, true);
                axisLeft.setXOffset(10);
                axisLeft.setTextColor(ContextCompat.getColor(context, R.color.light_gray));

                binding.lineChart.setExtraBottomOffset(10);
                binding.lineChart.getLegend().setEnabled(false);
                binding.lineChart.getDescription().setEnabled(false);
                binding.lineChart.getAxisLeft().setDrawGridLines(false);
                binding.lineChart.setBackgroundColor(ContextCompat.getColor(context, R.color.color_recycler_item));
                binding.lineChart.setDrawGridBackground(true);
                binding.lineChart.setGridBackgroundColor(ContextCompat.getColor(context, R.color.graph_color));

                binding.lineChart.setData(data);

            }
        }

        private int getMaxWeight(List<Set_exercise> set_exercises){
            int max = 0;
            for(Set_exercise set : set_exercises){
                if(set.getWeight() > max)
                    max = set.getWeight();

            }
            return max;
        }
    }

}
