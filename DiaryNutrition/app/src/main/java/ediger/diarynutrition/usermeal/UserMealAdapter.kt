package ediger.diarynutrition.usermeal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.databinding.ListMealUserItemBinding
import ediger.diarynutrition.util.hitTest
import java.util.*

class UserMealAdapter: RecyclerView.Adapter<MealViewHolder>(), DraggableItemAdapter<MealViewHolder> {

    private var mealList: MutableList<Meal> = mutableListOf()

    fun submitList(list: List<Meal>?) {
        list?.let {
            mealList = list.toMutableList()
            notifyDataSetChanged()
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ListMealUserItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal: Meal = mealList[position]

        holder.bind(meal)
    }

    override fun getItemId(position: Int): Long = mealList[position].id.toLong()

    override fun getItemCount(): Int = mealList.size

    override fun onCheckCanStartDrag(holder: MealViewHolder, position: Int, x: Int, y: Int): Boolean {
        return with(holder.binding) {
            val offsetX = container.left + (container.translationX + 0.5f).toInt()
            val offsetY = container.top + (container.translationY + 0.5f).toInt()

            imDragHandle.hitTest(x - offsetX, y - offsetY)
        }

    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) {
            return
        }
        val item = mealList.removeAt(fromPosition)
        mealList.add(toPosition, item)
    }

    override fun onGetItemDraggableRange(holder: MealViewHolder, position: Int): ItemDraggableRange? = null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int) = true

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

}

class MealViewHolder(
        val binding: ListMealUserItemBinding
) : AbstractDraggableItemViewHolder(binding.root) {

    fun bind(meal: Meal) {
        binding.meal = meal
        binding.executePendingBindings()
    }

}