package ediger.diarynutrition.summary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

import ediger.diarynutrition.R;

@SuppressLint("ViewConstructor")
public class ChartMarkerView extends MarkerView {

    private LineChart mChart;

    private TextView mDate;
    private TextView mProt;
    private TextView mFat;
    private TextView mCarbo;

    private LinearLayout mProtLayout;
    private LinearLayout mFatLayout;
    private LinearLayout mCarboLayout;

    public ChartMarkerView(Context context, int layoutResource, LineChart chart) {
        super(context, layoutResource);

        mChart = chart;
        mDate = findViewById(R.id.marker_date);
        mProt = findViewById(R.id.marker_prot);
        mFat = findViewById(R.id.marker_fat);
        mCarbo = findViewById(R.id.marker_carbo);

        mProtLayout = findViewById(R.id.prot_layout);
        mFatLayout = findViewById(R.id.fat_layout);
        mCarboLayout = findViewById(R.id.carbo_layout);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        List<ILineDataSet> sets = mChart.getData().getDataSets();
        sets.get(0).getEntryForIndex((int) e.getX()).getY();

        mDate.setText(String.valueOf((int) e.getX() + 1));
        mProt.setText(String.valueOf((int) sets.get(0).getEntryForIndex((int) e.getX()).getY()));
        mFat.setText(String.valueOf((int) sets.get(1).getEntryForIndex((int) e.getX()).getY()));
        mCarbo.setText(String.valueOf((int) sets.get(2).getEntryForIndex((int) e.getX()).getY()));

        mProtLayout.setVisibility(sets.get(0).isVisible() ? View.VISIBLE : View.GONE);
        mFatLayout.setVisibility(sets.get(1).isVisible() ? View.VISIBLE : View.GONE);
        mCarboLayout.setVisibility(sets.get(2).isVisible() ? View.VISIBLE : View.GONE);

        super.refreshContent(e, highlight);
    }
}
