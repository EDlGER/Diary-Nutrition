package ediger.diarynutrition.food

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.*
import ediger.diarynutrition.databinding.ActivityFoodBinding
import ediger.diarynutrition.util.hideKeyboard
import ediger.diarynutrition.util.setupSnackbar
import ediger.diarynutrition.util.showKeyboard

class FoodActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding

    private val viewModel: FoodViewModel by viewModels()

    private var isAdRemoved = false
    private var adView: AdView? = null
    private var initialLayoutComplete = false
    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        isAdRemoved = getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.edSearch.doOnTextChanged { text, _, _, _ ->
            text?.trim()?.let { viewModel.setQueryValue(text.toString()) }
        }

        setupViewPager()
        setupSnackbar()

        if (!isAdRemoved) {
            initAd()
        }

        this.showKeyboard(binding.edSearch)
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
                if (!isAdRemoved) {
                    viewModel.isAdVisible.value = when(position) {
                        USER_TAB_POSITION -> false
                        else -> true
                    }
                }

                binding.edSearch.setText("")
                this@FoodActivity.hideKeyboard(binding.root)
            }
        })
    }

    private fun initAd() {
        adView = AdView(applicationContext)

        binding.adViewContainer.addView(adView)

        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true

                adView?.apply {
                    // TODO: getString(R.string.banner_ad_inter_id)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    adSize = this@FoodActivity.adSize
                    loadAd(
                            AdRequest.Builder().build()
                    )
                }
            }
        }
    }

    private fun setupSnackbar() {
        binding.root.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
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

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onPause() {
        super.onPause()
        adView?.pause()
    }

    override fun onDestroy() {
        adView?.destroy()
        this.hideKeyboard(binding.root.findFocus())

        super.onDestroy()
    }

    companion object {
        const val USER_TAB_POSITION = 2

        fun getIntent(context: Context, date: Long): Intent =
            Intent(context, FoodActivity::class.java).apply { putExtra(ARG_DATE, date) }
    }
}