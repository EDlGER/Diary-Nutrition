package ediger.diarynutrition.Fragments.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.R;

/**
 * Created by root on 05.09.15.
 */
public class FoodTab extends Fragment {
    View rootview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.food_tab, container, false);


        return rootview;
    }
}
