package ediger.diarynutrition.food

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.KEY_PREF_UI_DEFAULT_TAB
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.ActivityFoodBinding
import ediger.diarynutrition.util.setupSnackbar

class FoodActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding

    private val viewModel: FoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.edSearch.doOnTextChanged { text, _, _, _ ->
            text?.trim()?.let { viewModel.setQueryValue(text.toString()) }
        }

        setupViewPager()
        setupSnackbar()

        binding.edSearch.requestFocus()
        val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun setupViewPager() {
        val tabs = generateTabs(this)
        binding.pager.adapter = FoodPagerAdapter(this@FoodActivity, tabs)

        binding.tabsLayout.setUpWithViewPager(binding.pager)

        val position = PreferenceHelper.getValue(KEY_PREF_UI_DEFAULT_TAB, String::class.java, "0").toInt()
        binding.pager.setCurrentItem(position, true)
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.edSearch.setText("")
                hideKeyboard()
            }
        })
    }

    private fun setupSnackbar() {
        binding.root.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun hideKeyboard() {
        val view = binding.root.findFocus()
        val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let{ im.hideSoftInputFromWindow(view.windowToken, 0) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_food, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {
            supportFragmentManager.beginTransaction().add(android.R.id.content, AddFoodDialog()).commit()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = with(BottomSheetBehavior.from(binding.fragmentContainer)) {
        if (state == BottomSheetBehavior.STATE_EXPANDED) {
            state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }
}