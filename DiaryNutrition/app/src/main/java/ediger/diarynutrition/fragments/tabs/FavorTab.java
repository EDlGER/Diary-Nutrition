package ediger.diarynutrition.fragments.tabs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.AddActivityNew;

/**
 * Created by root on 05.09.15.
 */
public class FavorTab extends Fragment {
    View rootview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.favor_tab, container, false);

        Toolbar toolbar = (Toolbar) rootview.findViewById(R.id.toolbar1);

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setElevation(0);
        setHasOptionsMenu(true);

        AddActivityNew activity = (AddActivityNew) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return rootview;
    }
}