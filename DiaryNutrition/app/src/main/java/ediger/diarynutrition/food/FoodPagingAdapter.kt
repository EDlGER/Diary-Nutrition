package ediger.diarynutrition.food

import android.view.*
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import ediger.diarynutrition.data.source.entities.Food

class FoodPagingAdapter : PagingDataAdapter<Food, FoodViewHolder>(FOOD_COMPARATOR) {

    var isDefaultQuery = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = getItem(position)
        food?.let { holder.bind(food, isDefaultQuery) }
    }

    companion object {
        private val FOOD_COMPARATOR = object : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem == newItem
            }
        }
    }
}

