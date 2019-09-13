package ediger.diarynutrition.diary.water;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.databinding.ListWaterItemBinding;

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    private WaterViewModel mViewModel;

    private List<Water> mWaterList;

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());

    WaterAdapter(WaterViewModel viewModel) {
        mViewModel = viewModel;
        mWaterList = Collections.emptyList();

        setHasStableIds(true);
    }

    void setWaterList(List<Water> water) {
        if (water != null) {
            mWaterList = water;
            notifyDataSetChanged();
        }
        //DiffUtils
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListWaterItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.list_water_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mWaterList.get(position).getDatetime());

        holder.mBinding.txtTime.setText(timeFormatter.format(calendar.getTime()));
        holder.mBinding.txtAmount.setText(String.valueOf(mWaterList.get(position).getAmount()));

        holder.mBinding.frameDelete.setOnClickListener(v ->
            mViewModel.deleteWater(mWaterList.get(position).getId()));
    }

    @Override
    public int getItemCount() {
        if (mWaterList != null) {
            return mWaterList.size();
        } else {
            return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ListWaterItemBinding mBinding;

        ViewHolder(ListWaterItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

}
