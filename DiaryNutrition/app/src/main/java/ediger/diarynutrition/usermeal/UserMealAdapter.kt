package ediger.diarynutrition.usermeal

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import ediger.diarynutrition.KEY_MEAL_HIDDEN
import ediger.diarynutrition.KEY_MEAL_ORDER
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Meal
import ediger.diarynutrition.databinding.ListMealUserItemBinding
import ediger.diarynutrition.util.hitTest

class UserMealAdapter(
        val onClickListener: (id: Int) -> Unit
): RecyclerView.Adapter<MealViewHolder>(), DraggableItemAdapter<MealViewHolder> {

    private var mealList: MutableList<Meal> = mutableListOf()

    private val hiddenMealsList: MutableList<Int>
        get() {
            val hiddenMealsString = PreferenceHelper.getValue(KEY_MEAL_HIDDEN, String::class.java, "[]")
            return Gson().fromJson(hiddenMealsString, Array<Int>::class.java).toMutableList()
        }

    fun submitList(list: List<Meal>?) {
        list?.let {
            mealList = it.toMutableList()
            notifyDataSetChanged()
            saveItemOrder()
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ListMealUserItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        return MealViewHolder(binding, onClickListener)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal: Meal = mealList[holder.bindingAdapterPosition]
        val isHidden = hiddenMealsList.contains(meal.id)

        val color = when(isHidden) { true -> R.color.text_inactive else -> R.color.text_medium}
        ImageViewCompat.setImageTintList(
                holder.binding.imMealType,
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, color))
        )

        holder.bind(meal, isHidden)
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

        saveItemOrder()
    }

    override fun onGetItemDraggableRange(holder: MealViewHolder, position: Int): ItemDraggableRange? = null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int) = true

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    private fun saveItemOrder() {
        val itemOrderString = Gson().toJson(mealList.map { it.id }.toIntArray())
        PreferenceHelper.setValue(KEY_MEAL_ORDER, itemOrderString)
    }

}

class MealViewHolder(
        val binding: ListMealUserItemBinding,
        val onClickListener: (id: Int) -> Unit
) : AbstractDraggableItemViewHolder(binding.root) {

    fun bind(meal: Meal, isHidden: Boolean) = with(binding){
        this.meal = meal
        this.isHidden = isHidden
        container.setOnClickListener {
            onClickListener.invoke(meal.id)
        }
        executePendingBindings()
    }

}