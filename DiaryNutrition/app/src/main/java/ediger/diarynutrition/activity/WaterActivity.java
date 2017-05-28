package ediger.diarynutrition.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.klinker.android.sliding.SlidingActivity;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.WaterAdapter;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by ediger on 20.05.17.
 */

public class WaterActivity extends SlidingActivity {

    private long date;
    private RecyclerView listWater;

    @Override
    public void init(Bundle savedInstanceState) {
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

        date = intent.getLongExtra("date", 0);

        WaterAdapter adapter = new WaterAdapter(AppContext.getDbDiary().getWaterData(date));

        listWater = (RecyclerView) findViewById(R.id.list_water);
        listWater.setLayoutManager(new LinearLayoutManager(this));
        listWater.setAdapter(adapter);
    }
}
