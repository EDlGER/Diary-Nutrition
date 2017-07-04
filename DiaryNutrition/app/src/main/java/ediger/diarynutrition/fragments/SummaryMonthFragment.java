package ediger.diarynutrition.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;
import ediger.diarynutrition.view.ChartMarkerView;
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by root on 23.11.16.
 */
public class SummaryMonthFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private View rootview;

    private long firstMonthDay;

    /** Параметры графика */
    private int numberOfColumns = 31;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("d", Locale.getDefault());

    private Calendar calendar = Calendar.getInstance();
    private AppCompatSpinner monthChange;

    private ColumnChartView chartCal;
    private ColumnChartView chartWater;
    private ColumnChartData dataCal;
    private ColumnChartData dataWater;

    private LineChart chartMacro;

    private AppCompatCheckBox chProt;
    private AppCompatCheckBox chFat;
    private AppCompatCheckBox chCarbo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_summary_month, container, false);

        chartCal = (ColumnChartView) rootview.findViewById(R.id.month_chart_cal);
        chartMacro = (LineChart) rootview.findViewById(R.id.month_chart_macro);
        chartWater = (ColumnChartView) rootview.findViewById(R.id.month_chart_water);
        monthChange = (AppCompatSpinner) rootview.findViewById(R.id.sp_month_change);

        chProt = (AppCompatCheckBox) rootview.findViewById(R.id.ch_prot);
        chProt.setOnCheckedChangeListener(this);

        chFat = (AppCompatCheckBox) rootview.findViewById(R.id.ch_fat);
        chFat.setOnCheckedChangeListener(this);

        chCarbo = (AppCompatCheckBox) rootview.findViewById(R.id.ch_carbo);
        chCarbo.setOnCheckedChangeListener(this);

        //Получение первого дня текущей недели
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        firstMonthDay = calendar.getTimeInMillis();

        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        monthChange.setAdapter(adapter);
        monthChange.setSelection(calendar.get(Calendar.MONTH));
        monthChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar c = (Calendar) calendar.clone();
                if (position > c.get(Calendar.MONTH)) {
                    c.add(Calendar.YEAR, -1);
                }
                c.set(Calendar.MONTH, position);

                firstMonthDay = c.getTimeInMillis();
                generateCalData();
                generateMacroData();
                generateWaterData();
                chartMacro.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chartInit();

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

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateCalData();
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateMacroData();
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateWaterData();
            }
        });

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

    private void chartInit() {
        IAxisValueFormatter formatter = new DaysValueFormatter();

        //Axis
        XAxis axisX = chartMacro.getXAxis();
        axisX.setDrawGridLines(false);
        axisX.setAxisMaximum(numberOfColumns);
        axisX.setAxisMinimum(-1);
        axisX.setTextColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
        axisX.setAxisLineColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
        axisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        axisX.setValueFormatter(formatter);
        axisX.setTextSize(12);

        YAxis axisY  = chartMacro.getAxisLeft();
        axisY.enableGridDashedLine(10f, 0f, 0f);
        axisY.setAxisMinimum(0);
        axisY.setGridColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
        axisY.setTextColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
        axisY.setAxisLineColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
        axisY.setTextSize(10);

        ChartMarkerView marker = new ChartMarkerView(getActivity(),
                R.layout.view_chart_marker, chartMacro);
        marker.setOffset(20, -20);
        marker.setChartView(chartMacro);
        chartMacro.setMarker(marker);

        chartMacro.getLegend().setEnabled(false);
        chartMacro.getAxisRight().setEnabled(false);
        chartMacro.getDescription().setEnabled(false);
        chartMacro.setDrawGridBackground(false);
        chartMacro.setTouchEnabled(true);
        chartMacro.setDragEnabled(false);
        chartMacro.setScaleEnabled(false);
        chartMacro.setBackgroundColor(ContextCompat.getColor(getActivity(),
                R.color.layout_standart_background));
        chartMacro.setGridBackgroundColor(ContextCompat.getColor(getActivity(),
                R.color.layout_standart_background));
    }

    private void generateCalData() {
        int amount;
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.setTimeInMillis(firstMonthDay);

        numberOfColumns = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsCal = new ArrayList<>();

        List<SubcolumnValue> valuesCal;

        Cursor cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());

        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
                valuesCal = new ArrayList<>();
                valuesCal.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                if (i % 5 == 0) {
                    axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(currentMonth.getTime())));
                }
                Column column = new Column(valuesCal);

                if (i == 0) {
                    String s = "       ";
                    column.setFormatter(new SimpleColumnChartValueFormatter()
                            .setPrependedText(s.toCharArray()));
                } else if (i == numberOfColumns - 1) {
                    String s = "       ";
                    column.setFormatter(new SimpleColumnChartValueFormatter()
                            .setAppendedText(s.toCharArray()));
                }

                column.setHasLabelsOnlyForSelected(true);

                columnsCal.add(column);
            }
            currentMonth.add(Calendar.DAY_OF_WEEK, 1);
            cursor.close();
        }
        cursor.close();

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);
        axisY.setTextSize(10);

        dataCal = new ColumnChartData(columnsCal);
        dataCal.setAxisXBottom(axisX);
        dataCal.setAxisYLeft(axisY);

        chartCal.setValueSelectionEnabled(true);
        chartCal.setColumnChartData(dataCal);
        chartCal.setZoomEnabled(false);
    }

    private void generateMacroData() {
        int amount;
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.setTimeInMillis(firstMonthDay);

        ArrayList<Entry> valuesProt = new ArrayList<>();
        ArrayList<Entry> valuesFat = new ArrayList<>();
        ArrayList<Entry> valuesCarbo = new ArrayList<>();

        Cursor cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());

        // Prot values
        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_PROT));
                valuesProt.add(new Entry(i, amount));
            }
            currentMonth.add(Calendar.DAY_OF_MONTH, 1);
            cursor.close();
        }

        // Fat values
        currentMonth.setTimeInMillis(firstMonthDay);
        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_FAT));
                valuesFat.add(new Entry(i, amount));
            }
            currentMonth.add(Calendar.DAY_OF_MONTH, 1);
            cursor.close();
        }

        // Carbo values
        currentMonth.setTimeInMillis(firstMonthDay);
        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayData(currentMonth.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_CARBO));
                valuesCarbo.add(new Entry(i, amount));
            }
            currentMonth.add(Calendar.DAY_OF_MONTH, 1);
            cursor.close();
        }

        cursor.close();

        LineDataSet set1, set2, set3;

        if (chartMacro.getData() != null &&
                chartMacro.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chartMacro.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chartMacro.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) chartMacro.getData().getDataSetByIndex(2);
            set1.setValues(valuesProt);
            set2.setValues(valuesFat);
            set3.setValues(valuesCarbo);

            chartMacro.getData().notifyDataChanged();
            chartMacro.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(valuesProt, "Prot");
            set1.setColor(ContextCompat.getColor(getActivity(), R.color.chart_green));
            set1.setCircleColor(ContextCompat.getColor(getActivity(), R.color.chart_green));
            set1.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
            set1.setLineWidth(2f);
            set1.setCircleRadius(4f);
            set1.setDrawCircleHole(false);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);
            set1.setDrawHorizontalHighlightIndicator(false);

            set2 = new LineDataSet(valuesFat, "Fat");
            set2.setColor(ContextCompat.getColor(getActivity(), R.color.chart_red));
            set2.setCircleColor(ContextCompat.getColor(getActivity(), R.color.chart_red));
            set2.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
            set2.setLineWidth(2f);
            set2.setCircleRadius(4f);
            set2.setDrawCircleHole(false);
            set2.setDrawCircles(false);
            set2.setDrawValues(false);
            set2.setDrawHorizontalHighlightIndicator(false);

            set3 = new LineDataSet(valuesCarbo, "Carbo");
            set3.setColor(ContextCompat.getColor(getActivity(), R.color.chart_orange));
            set3.setCircleColor(ContextCompat.getColor(getActivity(), R.color.chart_orange));
            set3.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.chart_label_color));
            set3.setLineWidth(2f);
            set3.setCircleRadius(4f);
            set3.setDrawCircleHole(false);
            set3.setDrawCircles(false);
            set3.setDrawValues(false);
            set3.setDrawHorizontalHighlightIndicator(false);

            LineData data = new LineData(set1, set2, set3);
            chartMacro.setData(data);
        }
    }

    private void generateWaterData() {
        int amount;
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.setTimeInMillis(firstMonthDay);

        numberOfColumns = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsWater = new ArrayList<>();

        List<SubcolumnValue> valuesWater;

        Cursor cursor = AppContext.getDbDiary().getDayWaterData(currentMonth.getTimeInMillis());

        for (int i = 0; i < numberOfColumns; i++) {
            cursor = AppContext.getDbDiary().getDayWaterData(currentMonth.getTimeInMillis());
            if (cursor.moveToFirst()) {
                amount = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SUM_AMOUNT));
                valuesWater = new ArrayList<>();
                valuesWater.add(new SubcolumnValue(amount,
                        ContextCompat.getColor(getActivity(), R.color.colorAccent)));
                if (i % 5 == 0) {
                    axisValues.add(new AxisValue(i).setLabel(dateFormatter.format(currentMonth.getTime())));
                }
                Column column = new Column(valuesWater);

                if (i == 0) {
                    String s = "       ";
                    column.setFormatter(new SimpleColumnChartValueFormatter()
                            .setPrependedText(s.toCharArray()));
                } else if (i == numberOfColumns - 1) {
                    String s = "       ";
                    column.setFormatter(new SimpleColumnChartValueFormatter()
                            .setAppendedText(s.toCharArray()));
                }

                column.setHasLabelsOnlyForSelected(true);

                columnsWater.add(column);
            }
            currentMonth.add(Calendar.DAY_OF_WEEK, 1);
            cursor.close();
        }
        cursor.close();

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);
        axisY.setTextSize(10);

        dataWater = new ColumnChartData(columnsWater);
        dataWater.setAxisXBottom(axisX);
        dataWater.setAxisYLeft(axisY);

        chartWater.setValueSelectionEnabled(true);
        chartWater.setColumnChartData(dataWater);
        chartWater.setZoomEnabled(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        List<ILineDataSet> sets = chartMacro.getData().getDataSets();

        sets.get(0).setVisible(chProt.isChecked());
        sets.get(1).setVisible(chFat.isChecked());
        sets.get(2).setVisible(chCarbo.isChecked());

        chartMacro.invalidate();
    }

    private class DaysValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Calendar currentMonth = Calendar.getInstance();
            currentMonth.setTimeInMillis(firstMonthDay);
            currentMonth.set(Calendar.DAY_OF_MONTH, (int) value + 1);

            if (currentMonth.getActualMaximum(Calendar.MONTH)==30 && value==30) {
                return null;
            }

            if (value % 5 == 0)
                return dateFormatter.format(currentMonth.getTime());

            return null;
        }
    }
}
