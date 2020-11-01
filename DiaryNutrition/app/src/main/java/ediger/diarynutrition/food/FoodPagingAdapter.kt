package ediger.diarynutrition.food

import android.view.*
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.databinding.ListFoodItemBinding

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

class FoodViewHolder(
        private val binding: ListFoodItemBinding
): RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

    init {
        binding.root.setOnClickListener {
            binding.food?.let { food ->
                navigateTo(food, it)
            }
        }
        binding.root.setOnCreateContextMenuListener(this)
    }

    fun bind(item: Food, defaultQuery: Boolean) {
        binding.apply {
            food = item
            isDefaultQuery = defaultQuery
            executePendingBindings()
        }
    }

    private fun navigateTo(food: Food, view: View) {
        // TODO: navigate to AddActivity(or Fragment) passing food item or id
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        val foodId = binding.food?.id ?: -1

        if (binding.food?.favorite == 1) {
            menu?.add(Menu.NONE, R.integer.action_context_favorite_remove, foodId, R.string.context_menu_favor_del)
        } else {
            menu?.add(Menu.NONE, R.integer.action_context_favorite_add, foodId, R.string.context_menu_favor)
            menu?.add(Menu.NONE, R.integer.action_context_delete, foodId, R.string.context_menu_del)
        }
        if (binding.food?.user == 1) {
            menu?.add(Menu.NONE, R.integer.action_context_change, foodId, R.string.context_menu_change)
        }

    }

    companion object {
        fun create(parent: ViewGroup): FoodViewHolder {
            val binding = ListFoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FoodViewHolder(binding)
        }
    }
}

