package ediger.diarynutrition.fragments;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by root on 27.02.16.
 */
public class WeightFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = -6;

    View rootview;
    ListView listWeight;
    Cursor cursor;
    String[] from;
    WeightAdapter weightAdapter;
    int[] to = {
            R.id.txtDate,
            R.id.txtWeight
    };

    private AppCompatRadioButton rbWeek;
    private AppCompatRadioButton rbMonth;

    private LineChartView chart;
    private LineChartData data;
    private int numberOfPoints = 7;
    private float maxWeightViewport = 40;
    private float minWeightViewport = 150;

    private boolean isWeek = true;
    private boolean isMonth = false;
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

        rootview = inflater.inflate(R.layout.fragment_weight, container, false);

        listWeight = (ListView) rootview.findViewById(R.id.listWeight);
        rbWeek = (AppCompatRadioButton) rootview.findViewById(R.id.rb_week);
        rbMonth = (AppCompatRadioButton) rootview.findViewById(R.id.rb_month);
        chart = (LineChartView) rootview.findViewById(R.id.weight_chart);

        rbWeek.setChecked(isWeek);
        rbMonth.setChecked(isMonth);

        generateData();
        resetViewport();

        rbWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWeek) {
                    isWeek = !isWeek;
                    isMonth = !isMonth;
                    rbWeek.setChecked(isWeek);
                    rbMonth.setChecked(isMonth);
                    generateData();
                    resetViewport();
                }
            }
        });
        rbMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMonth) {
                    isWeek = !isWeek;
                    isMonth = !isMonth;
                    rbWeek.setChecked(isWeek);
                    rbMonth.setChecked(isMonth);
                    generateData();
                    resetViewport();
                }
            }
        });

        cursor = AppContext.getDbDiary().getAllWeight();
        from = AppContext.getDbDiary().getListWeight();
        weightAdapter = new WeightAdapter(getActivity(), R.layout.fragment_weight, cursor, from, to, 0);

        listWeight.setAdapter(weightAdapter);
        registerForContextMenu(listWeight);

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootview;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.context_menu_del);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 1) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            AppContext.getDbDiary().delWeight(acmi.id);
            generateData();
            resetViewport();
            getLoaderManager().getLoader(LOADER_ID).forceLoad();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void resetViewport() {
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
        int monthMarker = 7;
        Cursor cursor = null;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM");


        if (isWeek) {
            cursor = AppContext.getDbDiary().getWeekWeight();
            numberOfPoints = 7;
        } else if (isMonth) {
            cursor = AppContext.getDbDiary().getMonthWeight();
            numberOfPoints = 30;
        }

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
            maxWeightViewport = weight;
            minWeightViewport = weight;

            for (int i = 1; i < numberOfPoints; i++) {
                if (cursor.moveToNext()) {
                    weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
                    date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));
                    calendar.setTimeInMillis(date);
                    values.add(new PointValue(i+1,weight).setTarget(i+1,weight).setLabel(String.format("%.1f", weight)));
                    if (numberOfPoints == 7) {
                        axisValues.add(new AxisValue(i+1).setLabel(dateFormatter.format(calendar.getTime())));
                    } else if (numberOfPoints == 30 && i == monthMarker) {
                        axisValues.add(new AxisValue(i+1).setLabel(dateFormatter.format(calendar.getTime())));
                        monthMarker = monthMarker + 7;
                    }

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(),AppContext.getDbDiary());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        weightAdapter.swapCursor(data);
        listWeight.setAdapter(weightAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class MyCursorLoader extends CursorLoader {
        DbDiary db;

        public MyCursorLoader(Context context, DbDiary db) {
            super(context);
            this.db = db;
        }

        @Override
        public  Cursor loadInBackground(){
            Cursor cursor = db.getAllWeight();
            return cursor;
        }
    }
}
