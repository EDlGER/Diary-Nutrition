package ediger.diarynutrition.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import ediger.diarynutrition.Consts;
import ediger.diarynutrition.R;
import ediger.diarynutrition.SlidingActivity;
import ediger.diarynutrition.adapters.WaterAdapter;
import ediger.diarynutrition.objects.AppContext;

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

        WaterAdapter adapter = new WaterAdapter(AppContext.getDbDiary().getWaterData(date), date);

        listWater = (RecyclerView) findViewById(R.id.list_water);
        listWater.setLayoutManager(new LinearLayoutManager(this));
        listWater.setAdapter(adapter);
    }
}
