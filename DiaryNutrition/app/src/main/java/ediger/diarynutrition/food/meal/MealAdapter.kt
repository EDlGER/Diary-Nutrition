package ediger.diarynutrition.food.meal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.data.source.entities.RecordAndFood
import ediger.diarynutrition.databinding.ListMealFoodItemBinding

class MealAdapter(
        private val onServingChangedCallback: OnServingChangedCallback
) : ListAdapter<RecordAndFood, MealFoodViewHolder>(FOOD_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealFoodViewHolder {
        return MealFoodViewHolder(
                ListMealFoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MealFoodViewHolder, position: Int) {
        var food= Food()
        var serving = 100

        currentList[position].record?.let { serving = it.serving }
        currentList[position].food?.let {
            food = Food(
                    it.name,
                    it.cal * serving / 100,
                    it.prot * serving / 100,
                    it.fat * serving / 100,
                    it.carbo * serving / 100
            )
        }
        holder.bind(food, serving, onServingChangedCallback)
    }

    companion object {
        private val FOOD_COMPARATOR = object : DiffUtil.ItemCallback<RecordAndFood>() {
            override fun areItemsTheSame(oldItem: RecordAndFood, newItem: RecordAndFood): Boolean {
                return (oldItem.record?.id == newItem.record?.id) &&
                        (oldItem.food?.id == newItem.record?.id)
            }

            override fun areContentsTheSame(oldItem: RecordAndFood, newItem: RecordAndFood): Boolean {
                return (oldItem.record == newItem.record) && (oldItem.food == newItem.food)
            }
        }
    }

}

class MealFoodViewHolder(
        val binding: ListMealFoodItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(food: Food, serving: Int, onServingChangedCallback: OnServingChangedCallback) {
        binding.onServingChangedCallback = onServingChangedCallback
        binding.food = food
        binding.serving = serving
        binding.executePendingBindings()
    }

}