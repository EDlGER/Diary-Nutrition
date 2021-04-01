package ediger.diarynutrition.food

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ediger.diarynutrition.R

class FoodPagerAdapter(
        fa: FragmentActivity,
        private val tabs: List<TabUiModel>
): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        val fragment = FoodFragment()
        val tabVariance = when (position) {
            0 -> FoodVariance.ALL
            1 -> FoodVariance.FAVORITES
            2 -> FoodVariance.USER
            else -> FoodVariance.ALL
        }
        fragment.arguments = bundleOf(FoodFragment.VARIANCE to tabVariance)

        return fragment
    }
}

fun generateTabs(context: Context): List<TabUiModel> {
    return listOf(
            TabUiModel(context.resources.getString(R.string.tab_products), R.drawable.ic_products),
            TabUiModel(context.resources.getString(R.string.tab_favorite), R.drawable.ic_favorite),
            TabUiModel(context.resources.getString(R.string.tab_user), R.drawable.ic_user)
    )
}

data class TabUiModel(val name: String, @DrawableRes val icon: Int)