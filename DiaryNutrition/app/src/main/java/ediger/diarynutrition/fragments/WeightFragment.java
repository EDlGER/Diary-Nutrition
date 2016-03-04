package ediger.diarynutrition.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.WeightAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.ValueFormatterHelper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.util.FloatUtils;
import lecho.lib.hellocharts.view.LineChartView;

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

    private LineChartView chart;
    private LineChartData data;
    private int numberOfPoints = 7;
    private float maxWeightViewport = 40;
    private float minWeightViewport = 150;


    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = true;


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
        mainActivity.title.setPadding(0, 25, 0, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.weight_layout, container, false);

        ListView listWeight = (ListView) rootview.findViewById(R.id.listWeight);
        chart = (LineChartView) rootview.findViewById(R.id.weight_chart);

        generateData();
        resetViewport();

        cursor = AppContext.getDbDiary().getAllWeight();
        from = AppContext.getDbDiary().getListWeight();
        weightAdapter = new WeightAdapter(getActivity(), R.layout.weight_layout, cursor, from, to, 0);

        listWeight.setAdapter(weightAdapter);


        return rootview;
    }



    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 40;
        v.top = 150;
        v.left = 0;
        v.right = numberOfPoints + 1;
        chart.setMaximumViewport(v);

        v.bottom = minWeightViewport - 2;
        v.top = maxWeightViewport + 2;
        chart.setCurrentViewport(v);
    }

    private void generateData() {
        float weight;
        long date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM");

        Cursor cursor = AppContext.getDbDiary().getWeekWeight();

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        List<AxisValue> axisValues = new ArrayList<>();

        List<PointValue> values = new ArrayList<>();
        if (cursor.moveToFirst()) {
            weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
            date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));
            calendar.setTimeInMillis(date);

            values.add(new PointValue(1, weight).setTarget(1, weight).setLabel(String.format("%.1f",weight)));
            axisValues.add(new AxisValue(1).setLabel(dateFormatter.format(calendar.getTime())));

            for (int i = 1; i < numberOfPoints; i++) {
                if (cursor.moveToNext()) {
                    weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
                    date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));
                    calendar.setTimeInMillis(date);
                    values.add(new PointValue(i+1,weight).setTarget(i+1,weight).setLabel(String.format("%.1f", weight)));
                    axisValues.add(new AxisValue(i+1).setLabel(dateFormatter.format(calendar.getTime())));

                    if (maxWeightViewport < weight) {
                        maxWeightViewport = weight;
                    }
                    if (minWeightViewport > weight) {
                        minWeightViewport = weight;
                    }
                }
            }
        }
        cursor.close();

        List<Line> lines = new ArrayList<Line>();
        Line line = new Line(values);
        line.setColor(getResources().getColor(R.color.colorAccent));
        line.setShape(shape);
        line.setFilled(isFilled);
        line.setHasLabels(hasLabels);
        line.setHasLines(hasLines);
        line.setHasPoints(hasPoints);
        lines.add(line);


        data = new LineChartData(lines);



        axisX.setValues(axisValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);


        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

    }
}
