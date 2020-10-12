package ediger.diarynutrition.food

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import ediger.diarynutrition.KEY_PREF_UI_DEFAULT_TAB
import ediger.diarynutrition.R
import ediger.diarynutrition.SlidingTabLayout
import ediger.diarynutrition.adapters.ViewPagerAdapter
import ediger.diarynutrition.settings.SettingsFragment

class OldFoodActivity : AppCompatActivity(), OnPageChangeListener {
    private val tabsNum = 3
    private var adapter: ViewPagerAdapter? = null
    private val titles = arrayOfNulls<CharSequence>(tabsNum)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_old)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val mAdView = findViewById<AdView>(R.id.adView)
        //AdRequest adRequest = new AdRequest.Builder().build();
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E8B8A18280F8D8867639D5CF594195AD")
                .build()
        var pref = getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
        val isAdsRemoved = pref.getBoolean(PREF_ADS_REMOVED, false)
        if (isAdsRemoved) {
            mAdView.isEnabled = false
            mAdView.visibility = View.GONE
        } else {
            mAdView.isEnabled = true
            mAdView.visibility = View.VISIBLE
            mAdView.loadAd(adRequest)
        }
        val toolbar = findViewById<View>(R.id.toolbar1) as Toolbar
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.elevation = 0f
        }
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        titles[0] = getString(R.string.tab_products)
        titles[1] = getString(R.string.tab_favorite)
        titles[2] = getString(R.string.tab_user)

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = ViewPagerAdapter(supportFragmentManager, titles, tabsNum)

        // Assigning ViewPager View and setting the adapter
        val pager = findViewById<ViewPager>(R.id.pager)
        pager.adapter = adapter
        invalidateFragmentMenus(pager.currentItem)
        pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
        pager.currentItem = pref.getString(KEY_PREF_UI_DEFAULT_TAB, "0")!!.toInt()

        // Assiging the Sliding Tab Layout View
        val tabs = findViewById<SlidingTabLayout>(R.id.tabs)
        tabs.setDistributeEvenly(true) // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer { ContextCompat.getColor(this@OldFoodActivity, R.color.tabsScrollColor) }

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        invalidateFragmentMenus(position)
    }

    private fun invalidateFragmentMenus(position: Int) {
        for (i in 0 until adapter!!.count) {
            adapter!!.getItem(i).setHasOptionsMenu(i == position)
        }
        invalidateOptionsMenu()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    companion object {
        private const val PREF_ADS_REMOVED = "ads_removed"
        private const val PREF_FILE_PREMIUM = "premium_data"
    }
}