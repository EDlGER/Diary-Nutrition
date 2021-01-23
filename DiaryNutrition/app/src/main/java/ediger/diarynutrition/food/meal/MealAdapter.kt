package ediger.diarynutrition.food.meal

import android.app.Activity
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.data.source.entities.RecordAndFood
import ediger.diarynutrition.databinding.ListMealFoodItemBinding
import ediger.diarynutrition.util.showKeyboard

class MealAdapter(
        private val onServingChangedCallback: OnServingChangedCallback
) : ListAdapter<RecordAndFood, MealFoodViewHolder>(FOOD_COMPARATOR) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealFoodViewHolder {
        return MealFoodViewHolder(
                ListMealFoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MealFoodViewHolder, position: Int) {
        var food = Food()
        var serving = 100

        currentList[holder.bindingAdapterPosition].record?.let { serving = it.serving }
        currentList[holder.bindingAdapterPosition].food?.let {
            food = Food(
                    it.name,
                    it.cal * serving / 100,
                    it.prot * serving / 100,
                    it.fat * serving / 100,
                    it.carbo * serving / 100
            ).apply { id = it.id }
        }

        holder.bind(food, serving, onServingChangedCallback)

        if (position == currentList.lastIndex) {
            holder.binding.edServing.requestFocus()
        }

        if (serving == 100 && !holder.binding.edServing.text.toString().contains("100")) {
            holder.clearText()
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].food?.id?.toLong() ?: -1

    companion object {
        private val FOOD_COMPARATOR = object : DiffUtil.ItemCallback<RecordAndFood>() {
            override fun areItemsTheSame(oldItem: RecordAndFood, newItem: RecordAndFood): Boolean {
                return oldItem.food?.id == newItem.food?.id
            }

            override fun areContentsTheSame(oldItem: RecordAndFood, newItem: RecordAndFood): Boolean {
                return (oldItem.record == newItem.record) && (oldItem.food == newItem.food)
            }
        }
    }

}

class MealFoodViewHolder(
        val binding: ListMealFoodItemBinding
) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

    init {
        binding.root.setOnCreateContextMenuListener(this)
    }

    fun bind(food: Food, serving: Int, onServingChangedCallback: OnServingChangedCallback) {
        binding.onServingChangedCallback = onServingChangedCallback
        binding.food = food
        binding.serving = serving
        binding.executePendingBindings()
    }

    fun clearText() {
        val callback = binding.onServingChangedCallback
        binding.onServingChangedCallback = null
        binding.edServing.setText("")
        binding.onServingChangedCallback = callback
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        val foodId = binding.food?.id ?: -1
        menu?.add(Menu.NONE, R.integer.action_context_remove, foodId, R.string.context_menu_remove)
    }

}