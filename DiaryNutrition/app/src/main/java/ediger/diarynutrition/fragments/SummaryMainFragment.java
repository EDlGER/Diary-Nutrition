package ediger.diarynutrition.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingTabLayout;
import ediger.diarynutrition.adapters.SummaryPagerAdapter;
import ediger.diarynutrition.adapters.ViewPagerAdapter;

/**
 * Created by root on 23.11.16.
 */
public class SummaryMainFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private View rootview;

    private int Numboftabs = 3;
    private ViewPager pager;
    private SummaryPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private CharSequence Titles[] = new CharSequence[Numboftabs];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_summary_main, container, false);

        Titles[0] = getString(R.string.tab_day);
        Titles[1] = getString(R.string.tab_week);
        Titles[2] = getString(R.string.tab_month);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new SummaryPagerAdapter(getActivity().getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) rootview.findViewById(R.id.pager_summary);
        pager.setAdapter(adapter);
        invalidateFragmentMenus(pager.getCurrentItem());

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) rootview.findViewById(R.id.tabs_summary);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getActivity() ,R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        return rootview;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void invalidateFragmentMenus(int position) {
        for(int i = 0; i < adapter.getCount(); i++) {
            adapter.getItem(i).setHasOptionsMenu(i == position);
        }
        getActivity().invalidateOptionsMenu();
    }


}
