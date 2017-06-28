package ediger.diarynutrition.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by root on 23.11.16.
 */
public class SummaryWeekFragment extends Fragment {
    private View rootview;

    private long firstWeekDay;

    /** Параметры графика */
    private int numberOfColumns = 7;
    private boolean hasLabels = false;

    private Calendar calendar = Calendar.getInstance();
    private AppCompatSpinner weekChange;

    private ColumnChartView chartCal;
    private ColumnChartView chartMacro;
    private ColumnChartView chartWater;
    private ColumnChartData dataCal;
    private ColumnChartData dataMacro;
    private ColumnChartData dataWater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_summary_week, container, false);

        chartCal = (ColumnChartView) rootview.findViewById(R.id.week_chart_cal);
        chartMacro = (ColumnChartView) rootview.findViewById(R.id.week_chart_macro);
        chartWater = (ColumnChartView) rootview.findViewById(R.id.week_chart_water);
        weekChange = (AppCompatSpinner) rootview.findViewById(R.id.sp_week_change);

        //Получение первого дня текущей недели
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        firstWeekDay = calendar.getTimeInMillis();
        final Calendar firstDayOfWeek = (Calendar) calendar.clone();
        Calendar lastDayOfWeek = (Calendar) calendar.clone();
        lastDayOfWeek.add(Calendar.DATE, 6);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        String[] values = new String[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = dateFormat.format(firstDayOfWeek.getTime()) + " - " + dateFormat.format(lastDayOfWeek.getTime());
            firstDayOfWeek.add(Calendar.DATE, -7);
            lastDayOfWeek.add(Calendar.DATE, -7);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekChange.setAdapter(adapter);

        weekChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar c = (Calendar) calendar.clone();
                c.add(Calendar.WEEK_OF_YEAR, position * -1);
                firstWeekDay = c.getTimeInMillis();
                generateCalData();
                generateMacroData();
                generateWaterData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chartCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasLabels = !hasLabels;
                generateCalData();
                generateMacroData();
                generateWaterData();
            }
        });

        chartWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasLabels = !hasLabels;
                generateCalData();
                generateMacroData();
                generateWaterData();
            }
        });

        generateCalData();
        generateMacroData();
        generateWaterData();

        return rootview;
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
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

    private void generateCalData() {
        int amount;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EE d", Locale.getDefault());
        Calendar currentWeek = Calendar.getInstance();
        currentWeek.setTimeInMillis(firstWeekDay);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsCal = new ArrayList<>();

        List<SubcolumnValue> valuesCal;

        Cursor cursor = AppContext.getDbDiary().getDayData(currentWeek.getTimeInMillis());

        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentWeek.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
                valuesCal = new ArrayList<>();
                valuesCal.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(currentWeek.getTime())));
                Column column = new Column(valuesCal);
                if (hasLabels) {
                    if (amount == 0) {
                        column.setHasLabels(false);
                    } else {
                        column.setHasLabels(true);
                    }
                } else {
                    column.setHasLabels(false);
                }

                columnsCal.add(column);
            }
            currentWeek.add(Calendar.DAY_OF_WEEK, 1);
        }
        cursor.close();

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);

        dataCal = new ColumnChartData(columnsCal);
        dataCal.setAxisXBottom(axisX);
        dataCal.setAxisYLeft(axisY);

        chartCal.setColumnChartData(dataCal);
        chartCal.setZoomEnabled(false);
    }

    private void generateMacroData() {
        int amount;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EE d", Locale.getDefault());
        Calendar currentWeek = Calendar.getInstance();
        currentWeek.setTimeInMillis(firstWeekDay);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columns = new ArrayList<>();

        List<SubcolumnValue> values;

        Cursor cursor = AppContext.getDbDiary().getDayData(currentWeek.getTimeInMillis());

        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentWeek.getTimeInMillis());
            if (cursor.moveToFirst()) {
                values = new ArrayList<>();
                axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(currentWeek.getTime())));

                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_CARBO));
                values.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.chart_orange)));

                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_PROT));
                values.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.chart_green)));

                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_FAT));
                values.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.chart_red)));

                Column column = new Column(values);
                column.setHasLabelsOnlyForSelected(true);
                columns.add(column);
            }
            currentWeek.add(Calendar.DAY_OF_WEEK, 1);
        }
        cursor.close();

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(3);

        dataMacro = new ColumnChartData(columns);
        dataMacro.setStacked(false);
        dataMacro.setAxisXBottom(axisX);
        dataMacro.setAxisYLeft(axisY);

        /*dataMacro.setValueLabelBackgroundAuto(false);
        dataMacro.setValueLabelBackgroundColor(ContextCompat.getColor(getActivity(),
                R.color.black_transpanent));
        dataMacro.setValueLabelsTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        dataMacro.setValueLabelTextSize(12);*/

        chartMacro.setColumnChartData(dataMacro);
        chartMacro.setZoomEnabled(false);
        chartMacro.setValueSelectionEnabled(true);
    }

    private void generateWaterData() {
        int amount;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EE d", Locale.getDefault());
        Calendar currentWeek = Calendar.getInstance();
        currentWeek.setTimeInMillis(firstWeekDay);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsWater = new ArrayList<>();

        List<SubcolumnValue> valuesWater;

        Cursor cursor = AppContext.getDbDiary().getDayWaterData(currentWeek.getTimeInMillis());

        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayWaterData(currentWeek.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SUM_AMOUNT));
                valuesWater = new ArrayList<>();
                valuesWater.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(currentWeek.getTime())));
                Column column = new Column(valuesWater);
                if (hasLabels) {
                    if (amount == 0) {
                        column.setHasLabels(false);
                    } else {
                        column.setHasLabels(true);
                    }
                } else {
                    column.setHasLabels(false);
                }

                columnsWater.add(column);
            }
            currentWeek.add(Calendar.DAY_OF_WEEK, 1);
        }
        cursor.close();

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);

        dataWater = new ColumnChartData(columnsWater);
        dataWater.setAxisXBottom(axisX);
        dataWater.setAxisYLeft(axisY);

        chartWater.setColumnChartData(dataWater);
        chartWater.setZoomEnabled(false);
    }

}
