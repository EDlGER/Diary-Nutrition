package ediger.diarynutrition.diary.adapters

import android.view.*
import android.view.View.OnCreateContextMenuListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.data.source.entities.MealAndRecords
import ediger.diarynutrition.data.source.entities.Record
import ediger.diarynutrition.databinding.ListRecordChildItemBinding
import ediger.diarynutrition.databinding.ListRecordGroupItemBinding
import ediger.diarynutrition.diary.adapters.RecordAdapter.ChildViewHolder
import ediger.diarynutrition.diary.adapters.RecordAdapter.GroupViewHolder

class RecordAdapter(
        private val itemManager: RecyclerViewExpandableItemManager
) : AbstractExpandableItemAdapter<GroupViewHolder, ChildViewHolder>() {

    private var recordsWithMeal: List<MealAndRecords> = emptyList()

    private var isContextMenuOpen = false

    fun setRecordList(recordList: List<MealAndRecords>?) {
        recordList?.let {
            recordsWithMeal = it
            notifyDataSetChanged()
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding: ListRecordGroupItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_record_group_item,
                parent, false)
        return GroupViewHolder(binding)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding: ListRecordChildItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.list_record_child_item,
                parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindGroupViewHolder(holder: GroupViewHolder, groupPosition: Int, viewType: Int) {
        var cal = 0f
        var prot = 0f
        var fat = 0f
        var carbo = 0f

        recordsWithMeal[groupPosition].records?.forEach {
            it.food?.let { food ->
                cal += food.cal
                prot += food.prot
                fat += food.fat
                carbo += food.carbo
            }
        }
        val food = Food("", cal, prot, fat, carbo)

        recordsWithMeal[groupPosition].meal?.let { meal ->
            holder.bind(meal, food, groupPosition)
        }

        if (holder.expandState.isUpdated) {
            holder.updateExpandState()
        }
    }

    override fun onBindChildViewHolder(holder: ChildViewHolder, groupPosition: Int, childPosition: Int, viewType: Int) {
        var record = Record(-1, -1, 100, -1)
        var food = Food("", 0f, 0f, 0f, 0f)

        recordsWithMeal[groupPosition].records?.let { records ->
            records[childPosition].record?.let { record = it }
            records[childPosition].food?.let { food = it }
        }

        holder.bind(
                record,
                food,
                groupPosition,
                childPosition,
                isLastChild = childPosition == getChildCount(groupPosition) - 1
        )
    }

    override fun getGroupCount(): Int = recordsWithMeal.size

    override fun getChildCount(groupPosition: Int): Int {
        return recordsWithMeal[groupPosition].records?.size ?: 0
    }

    override fun getGroupId(groupPosition: Int): Long {
        return recordsWithMeal[groupPosition].meal?.id?.toLong() ?: -1
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return recordsWithMeal[groupPosition].records?.get(childPosition)?.record?.id?.toLong() ?: -1
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: GroupViewHolder, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
        return (holder.itemView.isEnabled
                && !isContextMenuOpen
                && getChildCount(groupPosition) != 0)
    }


    fun getChildRecordData(groupPos: Int, childPos: Int): Record? {
        return recordsWithMeal[groupPos].records?.get(childPos)?.record
    }

    fun deleteChild(groupPos: Int, childPos: Int) {
        recordsWithMeal[groupPos].records?.let { list ->
            val newList = list.toMutableList().apply { removeAt(childPos) }
            recordsWithMeal[groupPos].records = newList

            itemManager.notifyChildItemRemoved(groupPos, childPos)
            itemManager.notifyGroupAndChildrenItemsChanged(groupPos)
            if (getChildCount(groupPos) == 0) {
                itemManager.collapseGroup(groupPos)
            }
        }
    }

    fun setContextMenuState(state: Boolean) {
        isContextMenuOpen = state
    }

    inner class GroupViewHolder(
            val binding: ListRecordGroupItemBinding
    ) : AbstractExpandableItemViewHolder(binding.root), OnCreateContextMenuListener {

        private var groupPosition: Int = -1

        init {
            binding.container.setOnCreateContextMenuListener(this)
        }

        fun bind(meal: Meal, food: Food, position: Int) {
            groupPosition = position
            binding.meal = meal
            binding.food = food
            binding.isExpanded = expandState.isExpanded
            binding.executePendingBindings()
        }

        fun updateExpandState() {
            val animateIndicator = expandState.hasExpandedStateChanged()
            binding.isExpanded = expandState.isExpanded
            binding.indicator.setExpandedState(expandState.isExpanded, animateIndicator)
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            binding.food?.let { food ->
                if (food.cal > 0) {
                    menu?.add(groupPosition, R.integer.action_context_copy, Menu.NONE, R.string.context_menu_copy)
                            ?.actionView = v
                }
            }
            menu?.add(groupPosition, R.integer.action_context_paste, Menu.NONE, R.string.context_menu_paste)?.actionView = v
            setContextMenuState(true)
        }
    }

    inner class ChildViewHolder(
            val binding: ListRecordChildItemBinding
    ) : AbstractExpandableItemViewHolder(binding.root), OnCreateContextMenuListener {

        private var groupPosition: Int = -1
        private var childPosition: Int = -1

        init {
            binding.container.setOnCreateContextMenuListener(this)
        }

        fun bind(record: Record, food: Food, groupPos: Int, childPos: Int, isLastChild: Boolean) {
            groupPosition = groupPos
            childPosition = childPos
            binding.record = record
            binding.food = food
            binding.isLastChild = isLastChild
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(groupPosition, R.integer.action_context_delete, childPosition, R.string.context_menu_remove)?.actionView = v
            menu?.add(groupPosition, R.integer.action_context_change_serving, childPosition, R.string.context_menu_change_serving)?.actionView = v
            menu?.add(groupPosition, R.integer.action_context_change_time, childPosition, R.string.context_menu_change_time)?.actionView = v
            setContextMenuState(true)
        }

    }

}