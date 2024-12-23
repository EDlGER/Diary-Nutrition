package ediger.diarynutrition.weight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.databinding.ListWeightItemBinding;

class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {

    private List<Weight> mWeight;
    private int mCurrentId;
    private View mEmptyView;

    WeightAdapter(View emptyView) {
        mWeight = Collections.emptyList();
        mEmptyView = emptyView;
    }

    void setWeightList(List<Weight> weight) {
        if (weight != null) {
            mWeight = weight;
            notifyDataSetChanged();

            if (weight.size() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListWeightItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_weight_item,
                parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setOnLongClickListener(v -> {
            setCurrentId(mWeight.get(position).getId());
            return false;
        });
        holder.mBinding.setWeight(mWeight.get(position));
        holder.mBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        if (mWeight != null) {
            return mWeight.size();
        } else {
            return 0;
        }
    }

    int getCurrentId() {
        return mCurrentId;
    }

    private void setCurrentId(int id) {
        mCurrentId = id;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ListWeightItemBinding mBinding;

        ViewHolder(ListWeightItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

}
