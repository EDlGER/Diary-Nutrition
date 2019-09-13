package ediger.diarynutrition.diary.adapters;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.composedadapter.ComposedChildAdapterTag;
import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter;

import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.ListRecordFooterBinding;
import ediger.diarynutrition.diary.DiaryViewModel;

import static ediger.diarynutrition.PreferenceHelper.KEY_PROGRAM_WATER;

public class WaterFooterAdapter extends AbstractHeaderFooterWrapperAdapter<WaterFooterAdapter.HeaderViewHolder,
        WaterFooterAdapter.FooterViewHolder> implements LifecycleEventObserver {

    private DiaryViewModel mViewModel;
    private LifecycleOwner mLifecycleOwner;
    private View.OnClickListener mOnClickListener;

    ListRecordFooterBinding mBinding;

    public WaterFooterAdapter(RecyclerView.Adapter adapter, LifecycleOwner lifecycleOwner, DiaryViewModel viewModel,
                              View.OnClickListener onClickListener) {
        setAdapter(adapter);
        mLifecycleOwner = lifecycleOwner;
        mViewModel = viewModel;
        mOnClickListener = onClickListener;
    }

    @Override
    public HeaderViewHolder onCreateHeaderItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @NonNull
    @Override
    public FooterViewHolder onCreateFooterItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.list_record_footer, parent, false);
        return new FooterViewHolder(mBinding);
    }

    @Override
    public void onBindHeaderItemViewHolder(@NonNull HeaderViewHolder holder, int localPosition) {

    }

    //TODO Footer
    @Override
    public void onBindFooterItemViewHolder(@NonNull FooterViewHolder holder, int localPosition) {
//        int goal = PreferenceHelper.getValue(KEY_PROGRAM_WATER, Integer.class, 1);
//        holder.binding.setGoal(goal);
        holder.binding.setLifecycleOwner(mLifecycleOwner);
        holder.binding.setViewModel(mViewModel);
        holder.binding.cardWater.setOnClickListener(mOnClickListener);
        holder.binding.executePendingBindings();

        // TODO: -----------------
        mLifecycleOwner.getLifecycle().addObserver(this);
    }

//    @Override
//    public boolean removeAdapter(@NonNull ComposedChildAdapterTag tag) {
//        mLifecycleOwner.getLifecycle().removeObserver(this);
//        return super.removeAdapter(tag);
//    }

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getFooterItemCount() {
        return 1;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            int goal = PreferenceHelper.getValue(KEY_PROGRAM_WATER, Integer.class, 1);
            mBinding.setGoal(goal);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        final ListRecordFooterBinding binding;

        FooterViewHolder(ListRecordFooterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
