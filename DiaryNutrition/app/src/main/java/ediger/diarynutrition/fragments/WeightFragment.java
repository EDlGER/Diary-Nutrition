package ediger.diarynutrition.fragments;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.activity.MainActivity;
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

public class WeightFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = -6;

    View rootview;

    /** Параметры графика */
    private int numberOfPoints = 7;
    private int maxDateViewport = 7;
    private float maxWeightViewport = 40;
    private float minWeightViewport = 150;

    private Cursor cursor;
    private int[] to = {
            R.id.txtDate,
            R.id.txtWeight
    };
    private ListView listWeight;
    private LineChartView chart;
    private WeightAdapter weightAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.fragment_weight, container, false);

        listWeight = (ListView) rootview.findViewById(R.id.listWeight);
        chart = (LineChartView) rootview.findViewById(R.id.weight_chart);
        AppCompatSpinner spInterval = (AppCompatSpinner) rootview.findViewById(R.id.sp_interval);
        spInterval.setSelection(0);

        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.weight_interval,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spInterval.setAdapter(adapter);
        spInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    numberOfPoints = 7;
                    generateData();
                    resetViewport();
                }

                if (position == 1) {
                    numberOfPoints = 30;
                    generateData();
                    resetViewport();
                }

                if (position == 2) {
                    numberOfPoints = 60;
                    generateData();
                    resetViewport();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listWeight.setEmptyView(rootview.findViewById(R.id.empty_list_weight));

        cursor = AppContext.getDbDiary().getAllWeight();
        String[] from = AppContext.getDbDiary().getListWeight();
        weightAdapter = new WeightAdapter(getActivity(), R.layout.fragment_weight, cursor, from, to, 0);

        listWeight.setAdapter(weightAdapter);
        registerForContextMenu(listWeight);

        generateData();
        resetViewport();

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootview;
    }

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

        if (getResources().getDisplayMetrics().density > 2.0) {
            mainActivity.title.setPadding(0, 40, 0, 0);
        } else {
            mainActivity.title.setPadding(0, 25, 0, 0);
        }

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
        v.right = maxDateViewport + 1;
        chart.setMaximumViewport(v);

        v.bottom = minWeightViewport - 2;
        v.top = maxWeightViewport + 2;
        chart.setCurrentViewport(v);
    }

    private void generateData() {
        float weight;
        long date;
        //Отслеживание дней
        int marker;
        //Показывать label через interval дней
        int interval;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -numberOfPoints);

        Cursor cursor = AppContext.getDbDiary()
                .getWeightFrom(Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis());

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);

        List<AxisValue> axisValues = new ArrayList<>();

        List<PointValue> values = new ArrayList<>();
        if (cursor.moveToFirst()) {
            weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
            date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));

            values.add(new PointValue(1, weight).setTarget(1, weight)
                    .setLabel(String.format(Locale.getDefault(), "%.1f", weight)));
            axisValues.add(new AxisValue(1).setLabel(dateFormatter.format(new Date(date))));
            maxWeightViewport = weight;
            minWeightViewport = weight;
            if (numberOfPoints > 7) {
                maxDateViewport = cursor.getCount();
            } else {
                maxDateViewport = numberOfPoints;
            }
            marker = maxDateViewport / 6;
            interval = maxDateViewport / 6;

            for (int i = 1; i < numberOfPoints; i++) {
                if (cursor.moveToNext()) {
                    weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
                    date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));
                    values.add(new PointValue(i+1, weight).setTarget(i+1, weight)
                            .setLabel(String.format(Locale.getDefault(), "%.1f", weight)));

                    if (numberOfPoints == 7) {
                        axisValues.add(new AxisValue(i+1).setLabel(dateFormatter.format(new Date(date))));
                    } else if (i == marker) {
                        axisValues.add(new AxisValue(i+1).setLabel(dateFormatter.format(new Date(date))));
                        marker += interval;
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

        List<Line> lines = new ArrayList<>();
        Line line = new Line(values);
        line.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        line.setShape(ValueShape.CIRCLE);
        line.setFilled(false);
        line.setHasLines(true);
        line.setHasPoints(true);
        line.setHasLabels(numberOfPoints == 7);
        line.setHasLabelsOnlyForSelected(numberOfPoints != 7);

        lines.add(line);

        LineChartData data = new LineChartData(lines);

        axisX.setValues(axisValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
        chart.setValueSelectionEnabled(numberOfPoints != 7);
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
            return db.getAllWeight();
        }
    }
}
