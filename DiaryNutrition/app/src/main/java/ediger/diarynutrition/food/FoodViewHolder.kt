package ediger.diarynutrition.food

import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ediger.diarynutrition.R
import ediger.diarynutrition.data.source.entities.Food
import ediger.diarynutrition.databinding.ListFoodItemBinding
import ediger.diarynutrition.food.meal.MealFragment

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
        var fragment: MealFragment?

        val fragmentManager = view.findFragment<FoodFragment>().activity?.supportFragmentManager

        fragment = fragmentManager?.findFragmentByTag(MealFragment.TAG) as? MealFragment

        if (fragment != null) {
            fragment.submitFood(food.id)
        } else {
            fragment = MealFragment()
            fragment.arguments = bundleOf(MealFragment.FOOD_ID to food.id)
            fragmentManager?.beginTransaction()?.apply {
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                replace(R.id.fragment_container, fragment, MealFragment.TAG)
                addToBackStack(MealFragment.TAG)
                commit()
            }
        }
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