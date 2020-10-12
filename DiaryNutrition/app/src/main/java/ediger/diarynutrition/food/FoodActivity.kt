package ediger.diarynutrition.food

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ediger.diarynutrition.KEY_PREF_UI_DEFAULT_TAB
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.ActivityFoodBinding

class FoodActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val tabs = generateTabs(this)
        binding.pager.adapter = FoodPagerAdapter(this@FoodActivity, tabs)

        binding.tabsLayout.setUpWithViewPager(binding.pager)

        val position = PreferenceHelper.getValue(KEY_PREF_UI_DEFAULT_TAB, String::class.java, "0").toInt()
        binding.pager.setCurrentItem(position, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_food, menu)
        return true
    }
}