package ediger.diarynutrition.diary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemState;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.util.Collections;
import java.util.List;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Food;
import ediger.diarynutrition.data.source.entities.MealAndRecords;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.RecordAndFood;
import ediger.diarynutrition.databinding.ListRecordChildItemBinding;
import ediger.diarynutrition.databinding.ListRecordGroupItemBinding;

public class RecordAdapter extends AbstractExpandableItemAdapter<
        RecordAdapter.GroupViewHolder, RecordAdapter.ChildViewHolder> {

    private List<MealAndRecords> mRecords;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
    private boolean isContextMenuOpen = false;

    private RecyclerViewExpandableItemManager mItemManager;

    public RecordAdapter(RecyclerViewExpandableItemManager itemManager, View.OnCreateContextMenuListener listener) {
        mItemManager = itemManager;
        mOnCreateContextMenuListener = listener;
        mRecords = Collections.emptyList();

        setHasStableIds(true);
    }

    public void setRecordList(List<MealAndRecords> recordList) {
        if (recordList != null) {
            mRecords = recordList;
            mItemManager.collapseAll();
            notifyDataSetChanged();
        }
        //DiffUtil?
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        ListRecordGroupItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_record_group_item,
                parent, false);
        return new GroupViewHolder(binding);
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        ListRecordChildItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_record_child_item,
                parent, false);
        return new ChildViewHolder(binding);
    }

    @Override
    public void onBindGroupViewHolder(@NonNull GroupViewHolder holder, int groupPosition, int viewType) {
        holder.binding.container.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
        holder.binding.setMeal(mRecords.get(groupPosition).getMeal());

        int cal = 0;
        float prot = 0;
        float fat = 0;
        float carbo = 0;

        List<RecordAndFood> records = mRecords.get(groupPosition).getRecords();

        for (RecordAndFood record : records) {
            cal += record.getFood().getCal();
            prot += record.getFood().getProt();
            fat += record.getFood().getFat();
            carbo += record.getFood().getCarbo();
        }
        Food food = new Food("", cal, prot, fat, carbo);
        holder.binding.setFood(food);

        final ExpandableItemState expandState = holder.getExpandState();
        boolean animateIndicator = expandState.hasExpandedStateChanged();

        if (expandState.isUpdated()) {
            holder.binding.setIsExpanded(expandState.isExpanded());
            holder.binding.indicator.setExpandedState(expandState.isExpanded(), animateIndicator);
        }

        holder.binding.executePendingBindings();
    }

    @Override
    public void onBindChildViewHolder(@NonNull ChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
        holder.binding.container.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
        holder.binding.setRecord(mRecords.get(groupPosition).getRecords().get(childPosition).getRecord());
        holder.binding.setFood(mRecords.get(groupPosition).getRecords().get(childPosition).getFood());

        if (childPosition == getChildCount(groupPosition) - 1) {
            //isLastChild
            holder.binding.setIsLastChild(true);
        } else {
            holder.binding.setIsLastChild(false);
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getGroupCount() {
        return mRecords.size();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mRecords.get(groupPosition).getRecords().size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mRecords.get(groupPosition).getMeal().getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mRecords.get(groupPosition).getRecords().get(childPosition).getRecord().getId();
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(@NonNull GroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return holder.itemView.isEnabled()
                && !isContextMenuOpen
                && getChildCount(groupPosition) != 0;

    }

    public Record getChildRecordData(int groupPos, int childPos) {
        Record childData = mRecords.get(groupPos).getRecords().get(childPos).getRecord();
        return new Record(childData.getMealId(),
                childData.getFoodId(),
                childData.getServing(),
                childData.getDatetime());
    }

    public void deleteChild(int groupPos, int childPos) {
        mRecords.get(groupPos).getRecords().remove(childPos);
        mItemManager.notifyChildItemRemoved(groupPos, childPos);
        mItemManager.notifyGroupAndChildrenItemsChanged(groupPos);
        if (getChildCount(groupPos) == 0) {
            mItemManager.collapseGroup(groupPos);
        }
    }

    public void setContextMenuState(boolean state) {
        isContextMenuOpen = state;
    }

    static class GroupViewHolder extends AbstractExpandableItemViewHolder {
        final ListRecordGroupItemBinding binding;

        GroupViewHolder(@NonNull ListRecordGroupItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ChildViewHolder extends AbstractExpandableItemViewHolder {
        final ListRecordChildItemBinding binding;

        ChildViewHolder(@NonNull ListRecordChildItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
