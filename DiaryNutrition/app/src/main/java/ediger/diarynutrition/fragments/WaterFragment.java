package ediger.diarynutrition.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.WaterAdapter;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by ediger on 19.05.17.
 */

public class WaterFragment extends Fragment {

    private long date;
    private RecyclerView listWater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_water, container, false);

        date = getArguments().getLong("date", 0);

        WaterAdapter adapter = new WaterAdapter(AppContext.getDbDiary().getWaterData(date));

        listWater = (RecyclerView) root.findViewById(R.id.list_water);
        listWater.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listWater.setLayoutManager(layoutManager);

        return root;
    }
}
