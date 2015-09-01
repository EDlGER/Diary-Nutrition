package ediger.diarynutrition.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.R;

/**
 * Created by Ediger on 03.05.2015.
 */
public class food_fragment extends Fragment {
    View rootview;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.food_layout,container,false);
        return rootview;
    }
}
