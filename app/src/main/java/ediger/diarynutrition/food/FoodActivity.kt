package ediger.diarynutrition.food

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.*
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.ActivityFoodBinding
import ediger.diarynutrition.util.hideKeyboard
import ediger.diarynutrition.util.setupSnackbar
import ediger.diarynutrition.util.showKeyboard

class FoodActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding

    private val viewModel: FoodViewModel by viewModels()

    private var isAdRemoved = false
    private var adView: AdView? = null
    private var rewardedAd: RewardedAd? = null
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
                .getBoolean(PREF_PREMIUM, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.edSearch.doOnTextChanged { text, _, _, _ ->
            text?.trim()?.let { viewModel.setQueryValue(text.toString()) }
        }

        setupViewPager()
        setupSnackbar()

        if (!isAdRemoved) {
            initBannerAd()
            initRewardedAd()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val sheetBehavior = BottomSheetBehavior.from(binding.fragmentContainer)
                if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        this.showKeyboard(binding.edSearch)
    }

    fun requestSearch() {
        binding.edSearch.setText("")
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
                //this@FoodActivity.hideKeyboard(binding.root)
            }
        })
    }

    private fun initBannerAd() {
        adView = AdView(applicationContext)

        binding.adViewContainer.addView(adView)

        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true

                adView?.apply {
                    // TODO: Change adUnitId to getString(R.string.banner_ad_inter_id)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    setAdSize(this@FoodActivity.adSize)
                    loadAd(
                            AdRequest.Builder().build()
                    )
                }
            }
        }
    }

    private fun initRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        // TODO: Change addUnitId to getString(R.string.ad_rewarded_user_food)
        RewardedAd.load(applicationContext, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded ad was loaded")
                rewardedAd = ad
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                rewardedAd = null
            }
        })

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
                rewardedAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed")
            }
        }
    }

    private fun setupSnackbar() {
        binding.root.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_food, menu)
        return true
    }

    // TODO: Delete options and add this code to the FAB click listener
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {
            if (viewModel.userFoodConstraint <= 0 && rewardedAd != null) {
                MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.dialog_title_food_reward_ad))
                        .setMessage(getString(R.string.dialog_message_food_ad))
                        .setPositiveButton(getString(R.string.dialog_watch_ad)) { _, _ ->
                            rewardedAd?.show(this) { rewardItem: RewardItem ->
                                viewModel.userFoodConstraint = rewardItem.amount
                                showAddFoodDialog()
                                viewModel.showSnackbarMessage(R.string.message_ad_reward_success)
                            }
                        }
                        .setNeutralButton(getString(R.string.dialog_premium)) { dialog, _ ->
                            dialog.dismiss()
                            // TODO: show Premium fragment
                        }
                        .setNegativeButton(getString(R.string.action_back)) { dialog, _ -> dialog.dismiss()}
                        .show()
                return false
            } else {
                showAddFoodDialog()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddFoodDialog() {
        supportFragmentManager.beginTransaction()
                .add(android.R.id.content, AddFoodDialog())
                .commit()
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
        const val TAG = "FoodActivity"
        const val USER_TAB_POSITION = 2

        fun getIntent(context: Context, date: Long): Intent =
            Intent(context, FoodActivity::class.java).apply { putExtra(ARG_DATE, date) }
    }
}