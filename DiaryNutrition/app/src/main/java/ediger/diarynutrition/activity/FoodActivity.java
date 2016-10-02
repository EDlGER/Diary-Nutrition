package ediger.diarynutrition.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingTabLayout;
import ediger.diarynutrition.adapters.ViewPagerAdapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class FoodActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private int Numboftabs = 3;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private CharSequence Titles[]= { "База данных","Добавленные", "Избранные" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(0);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        invalidateFragmentMenus(pager.getCurrentItem());

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
