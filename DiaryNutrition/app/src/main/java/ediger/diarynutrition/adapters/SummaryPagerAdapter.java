package ediger.diarynutrition.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ediger.diarynutrition.fragments.SummaryDayFragment;
import ediger.diarynutrition.fragments.SummaryMonthFragment;
import ediger.diarynutrition.fragments.SummaryWeekFragment;

/**
 * Created by root on 23.11.16.
 */
public class SummaryPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public SummaryPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        // if the position is 0 we are returning the First tab
        if(position == 0)
            return new SummaryDayFragment();
        else if (position == 1)
            return new SummaryWeekFragment();
        else
            return new SummaryMonthFragment();

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}



