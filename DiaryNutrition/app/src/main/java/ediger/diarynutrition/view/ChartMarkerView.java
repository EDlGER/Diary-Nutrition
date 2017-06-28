package ediger.diarynutrition.view;

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

/**
 * Created by ediger on 23.06.17.
 */

public class ChartMarkerView extends MarkerView {

    private LineChart chart;

    private TextView date;
    private TextView prot;
    private TextView fat;
    private TextView carbo;

    private LinearLayout protLayout;
    private LinearLayout fatLayout;
    private LinearLayout carboLayout;

    public ChartMarkerView(Context context, int layoutResource, LineChart chart) {
        super(context, layoutResource);

        this.chart = chart;
        date = (TextView) findViewById(R.id.marker_date);
        prot = (TextView) findViewById(R.id.marker_prot);
        fat= (TextView) findViewById(R.id.marker_fat);
        carbo = (TextView) findViewById(R.id.marker_carbo);

        protLayout = (LinearLayout) findViewById(R.id.prot_layout);
        fatLayout = (LinearLayout) findViewById(R.id.fat_layout);
        carboLayout = (LinearLayout) findViewById(R.id.carbo_layout);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        List<ILineDataSet> sets = chart.getData().getDataSets();
        sets.get(0).getEntryForIndex((int) e.getX()).getY();

        date.setText(String.valueOf((int) e.getX() + 1));
        prot.setText(String.valueOf((int) sets.get(0).getEntryForIndex((int) e.getX()).getY()));
        fat.setText(String.valueOf((int) sets.get(1).getEntryForIndex((int) e.getX()).getY()));
        carbo.setText(String.valueOf((int) sets.get(2).getEntryForIndex((int) e.getX()).getY()));

        protLayout.setVisibility(sets.get(0).isVisible() ? View.VISIBLE : View.GONE);
        fatLayout.setVisibility(sets.get(1).isVisible() ? View.VISIBLE : View.GONE);
        carboLayout.setVisibility(sets.get(2).isVisible() ? View.VISIBLE : View.GONE);

        super.refreshContent(e, highlight);
    }
}
