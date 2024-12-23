package ediger.diarynutrition.summary;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingTabLayout;

public class SummaryFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private SummaryPagerAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_main, container, false);

        CharSequence[] titles = {
                getString(R.string.tab_day),
                getString(R.string.tab_week),
                getString(R.string.tab_month)
        };

        mAdapter =  new SummaryPagerAdapter(getActivity().getSupportFragmentManager(), titles, titles.length);

        ViewPager pager = rootView.findViewById(R.id.view_pager);
        pager.setAdapter(mAdapter);
        invalidateFragmentMenus(pager.getCurrentItem());

        SlidingTabLayout tabs = rootView.findViewById(R.id.tab_layout);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(position ->
                ContextCompat.getColor(getActivity(), R.color.tabsScrollColor));
        tabs.setViewPager(pager);

        return rootView;

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
        for(int i = 0; i < mAdapter.getCount(); i++) {
            mAdapter.getItem(i).setHasOptionsMenu(i == position);
        }
        getActivity().invalidateOptionsMenu();
    }


}
