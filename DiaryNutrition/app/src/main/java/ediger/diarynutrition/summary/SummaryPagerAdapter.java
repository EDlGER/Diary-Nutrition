package ediger.diarynutrition.summary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import ediger.diarynutrition.summary.tabs.DayFragment;
import ediger.diarynutrition.summary.tabs.MonthFragment;
import ediger.diarynutrition.summary.tabs.WeekFragment;

public class SummaryPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] mTitles;
    private int mNumbOfTabs;

    SummaryPagerAdapter(FragmentManager fm, CharSequence[] titles, int numOfTabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mTitles = titles;
        this.mNumbOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if(position == 0)
            return new DayFragment();
        else if (position == 1)
            return new WeekFragment();
        else
            return new MonthFragment();

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return mNumbOfTabs;
    }

}



