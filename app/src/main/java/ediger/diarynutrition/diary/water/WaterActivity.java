package ediger.diarynutrition.diary.water;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingActivity;
import ediger.diarynutrition.data.source.entities.Water;

public class WaterActivity extends SlidingActivity {

    public static String ARG_DATE = "date";

    private WaterViewModel mViewModel;
    private WaterAdapter mAdapter;

    @Override
    public void init(Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(WaterViewModel.class);

        setTitle("Water");
        disableHeader();

        setPrimaryColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );

        setContent(R.layout.activity_water);

        Intent intent = getIntent();
        expandFromPoints(
                intent.getIntExtra(Consts.ARG_EXPANSION_LEFT_OFFSET, 0),
                intent.getIntExtra(Consts.ARG_EXPANSION_TOP_OFFSET, 0),
                intent.getIntExtra(Consts.ARG_EXPANSION_VIEW_WIDTH, 0),
                intent.getIntExtra(Consts.ARG_EXPANSION_VIEW_HEIGHT, 0)
        );

        long date = intent.getLongExtra(ARG_DATE, 0);
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(date);
        mViewModel.setDate(day);

        setupRecycler();
        subscribeRecycler(mViewModel.getWater());

    }

    private void setupRecycler() {
        mAdapter = new WaterAdapter(mViewModel);

        RecyclerView listWater = findViewById(R.id.list_water);
        listWater.setLayoutManager(new LinearLayoutManager(this));
        listWater.setAdapter(mAdapter);
    }

    private void subscribeRecycler(LiveData<List<Water>>liveData) {
        liveData.observe(this, water -> mAdapter.setWaterList(water));
    }


}
