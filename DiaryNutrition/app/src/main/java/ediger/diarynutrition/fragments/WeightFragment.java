package ediger.diarynutrition.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.WeightAdapter;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 27.02.16.
 */
public class WeightFragment extends Fragment {
    View rootview;
    Cursor cursor;
    String[] from;
    WeightAdapter weightAdapter;
    int[] to = {
            R.id.txtDate,
            R.id.txtWeight
    };

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!getUserVisibleHint()) {
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.menuMultipleActions.setVisibility(View.INVISIBLE);

        mainActivity.datePicker.setVisibility(View.INVISIBLE);
        mainActivity.title.setPadding(0,25,0,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.weight_layout, container, false);

        ListView listWeight = (ListView) rootview.findViewById(R.id.listWeight);


        cursor = AppContext.getDbDiary().getAllWeight();
        from = AppContext.getDbDiary().getListWeight();
        weightAdapter = new WeightAdapter(getActivity(), R.layout.weight_layout, cursor, from, to, 0);

        listWeight.setAdapter(weightAdapter);


        return rootview;
    }
}
