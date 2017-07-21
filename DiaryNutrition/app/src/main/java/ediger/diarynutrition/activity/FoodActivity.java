package ediger.diarynutrition.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingTabLayout;
import ediger.diarynutrition.adapters.ViewPagerAdapter;
import ediger.diarynutrition.fragments.SettingsFragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class FoodActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private static String PREF_ADS_REMOVED = "ads_removed";
    private static final String PREF_FILE_PREMIUM = "premium_data";

    private boolean isAdsRemoved;
    private int Numboftabs = 3;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private CharSequence Titles[] = new CharSequence[Numboftabs];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E8B8A18280F8D8867639D5CF594195AD")
                .build();

        SharedPreferences pref = getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE);
        isAdsRemoved = pref.getBoolean(PREF_ADS_REMOVED, false);

        if (isAdsRemoved) {
            mAdView.setEnabled(false);
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setEnabled(true);
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(adRequest);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(0);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Titles[0] = getString(R.string.tab_database);
        Titles[1] = getString(R.string.tab_favorite);
        Titles[2] = getString(R.string.tab_user);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        invalidateFragmentMenus(pager.getCurrentItem());


        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        pager.setCurrentItem(Integer.parseInt(pref.getString(SettingsFragment.KEY_PREF_UI_DEFAULT_TAB, "0")));

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(FoodActivity.this ,R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        invalidateFragmentMenus(position);
    }

    private void invalidateFragmentMenus(int position) {
        for(int i = 0; i < adapter.getCount(); i++) {
            adapter.getItem(i).setHasOptionsMenu(i == position);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
