package ediger.diarynutrition.summary.tabs;

import android.animation.AnimatorInflater;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

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
import java.util.Objects;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.databinding.FragmentSummaryWeekBinding;
import ediger.diarynutrition.summary.ChartMarkerView;
import ediger.diarynutrition.summary.SummaryViewModel;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;

public class WeekFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{

    private SummaryViewModel mViewModel;

    private Calendar mFirstDayOfWeek = Calendar.getInstance();

    private FragmentSummaryWeekBinding mBinding;

    private boolean hasLabels = false;

    private final int mNumberOfDays = 7;

    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("EE d", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary_week, container, false);

        mFirstDayOfWeek.set(Calendar.DAY_OF_WEEK, mFirstDayOfWeek.getFirstDayOfWeek());

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        mViewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary.size() == mNumberOfDays) {
                generateCalData(summary);
                generateMacroData(summary);
                mBinding.chartMacro.invalidate();
            }
        });
        mViewModel.getWater().observe(getViewLifecycleOwner(), water -> {
            if (water.size() == mNumberOfDays) {
                generateWaterData(water);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.chProt.setOnCheckedChangeListener(this);
        mBinding.chFat.setOnCheckedChangeListener(this);
        mBinding.chCarbo.setOnCheckedChangeListener(this);

        //Setup spinner adapter values
        final Calendar firstDay = (Calendar) mFirstDayOfWeek.clone();
        final Calendar lastDay = (Calendar) mFirstDayOfWeek.clone();
        lastDay.add(Calendar.DATE, 6);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        String[] values = new String[4];
        for (int i = 0; i < values.length; i++) {
            values[i] = dateFormat.format(firstDay.getTime()) + " - " + dateFormat.format(lastDay.getTime());
            firstDay.add(Calendar.DATE, -7);
            lastDay.add(Calendar.DATE, -7);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spWeekChange.setAdapter(adapter);

        final Calendar startOfCurrentWeek = (Calendar) mFirstDayOfWeek.clone();

        mBinding.spWeekChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar c = (Calendar) startOfCurrentWeek.clone();
                c.add(Calendar.WEEK_OF_YEAR, position * -1);
                mFirstDayOfWeek.setTime(c.getTime());
                mViewModel.setPeriod(mFirstDayOfWeek, mNumberOfDays);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.chartCal.setOnClickListener(v -> {
            hasLabels = !hasLabels;
            mViewModel.setPeriod(mFirstDayOfWeek, mNumberOfDays);
        });

        mBinding.chartWater.setOnClickListener(v -> {
            hasLabels = !hasLabels;
            mViewModel.setPeriod(mFirstDayOfWeek, mNumberOfDays);
        });

        chartInit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setPeriod(mFirstDayOfWeek, mNumberOfDays);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) requireActivity()).appBar
                    .setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getActivity(), R.animator.appbar_unelevated_animator)
                    );
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) requireActivity()).appBar
                    .setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getActivity(), R.animator.appbar_elevated_animator)
                    );
        }
    }

    private void chartInit() {
        IAxisValueFormatter formatter = new WeekFragment.DaysValueFormatter();

        //Axis
        XAxis axisX = mBinding.chartMacro.getXAxis();
        axisX.setDrawGridLines(false);
        axisX.setAxisMaximum(mNumberOfDays - 1);
        axisX.setAxisMinimum(0);
        axisX.setTextColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        axisX.setAxisLineColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        axisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        axisX.setValueFormatter(formatter);
        axisX.setTextSize(12);

        YAxis axisY  = mBinding.chartMacro.getAxisLeft();
        axisY.enableGridDashedLine(10f, 0f, 0f);
        axisY.setAxisMinimum(0);
        axisY.setGridColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        axisY.setTextColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        axisY.setAxisLineColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        axisY.setTextSize(10);

        ChartMarkerView marker = new ChartMarkerView(requireActivity(),
                R.layout.view_chart_marker, mBinding.chartMacro);
        marker.setOffset(20, -20);
        marker.setChartView(mBinding.chartMacro);
        mBinding.chartMacro.setMarker(marker);

        mBinding.chartMacro.getLegend().setEnabled(false);
        mBinding.chartMacro.getAxisRight().setEnabled(false);
        mBinding.chartMacro.getDescription().setEnabled(false);
        mBinding.chartMacro.setDrawGridBackground(false);
        mBinding.chartMacro.setTouchEnabled(true);
        mBinding.chartMacro.setDragEnabled(false);
        mBinding.chartMacro.setScaleEnabled(false);
        mBinding.chartMacro.setBackgroundColor(ContextCompat.getColor(requireActivity(),
                R.color.background_standard));
        mBinding.chartMacro.setGridBackgroundColor(ContextCompat.getColor(requireActivity(),
                R.color.background_standard));
    }

    private void generateCalData(List<Summary> summaryList) {
        float amount;
        int axisIndex = 0;
        Calendar currentWeek = (Calendar) mFirstDayOfWeek.clone();

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsCal = new ArrayList<>();

        List<SubcolumnValue> valuesCal;

        for (Summary summary : summaryList ) {
            amount = summary.getCal();
            valuesCal = new ArrayList<>();
            valuesCal.add(new SubcolumnValue(amount,
                    ContextCompat.getColor(requireActivity(), R.color.colorAccent)));
            axisValues.add(new AxisValue(axisIndex++).setLabel(mDateFormatter.format(currentWeek.getTime())));
            Column column = new Column(valuesCal);
            column.setHasLabels(hasLabels && amount != 0);
            columnsCal.add(column);

            currentWeek.add(Calendar.DAY_OF_WEEK, 1);
        }

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);

        ColumnChartData dataCal = new ColumnChartData(columnsCal);
        dataCal.setAxisXBottom(axisX);
        dataCal.setAxisYLeft(axisY);

        mBinding.chartCal.setColumnChartData(dataCal);
        mBinding.chartCal.setZoomEnabled(false);
    }

    private void generateMacroData(List<Summary> summaryList) {

        ArrayList<Entry> valuesProt = new ArrayList<>();
        ArrayList<Entry> valuesFat = new ArrayList<>();
        ArrayList<Entry> valuesCarbo = new ArrayList<>();

        int i = 0;
        for (Summary summary : summaryList) {
            valuesProt.add(new Entry(i, summary.getProt()));
            valuesFat.add(new Entry(i, summary.getFat()));
            valuesCarbo.add(new Entry(i, summary.getCarbo()));
            i++;
        }

        LineDataSet set1, set2, set3;

        //For updating the values
        if (mBinding.chartMacro.getData() != null &&
                mBinding.chartMacro.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mBinding.chartMacro.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mBinding.chartMacro.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) mBinding.chartMacro.getData().getDataSetByIndex(2);
            set1.setValues(valuesProt);
            set2.setValues(valuesFat);
            set3.setValues(valuesCarbo);

            mBinding.chartMacro.getData().notifyDataChanged();
            mBinding.chartMacro.notifyDataSetChanged();

            return;
        }
        set1 = getDefaultLineDataSet(valuesProt, getString(R.string.macro_prot));
        set1.setColor(ContextCompat.getColor(requireActivity(), R.color.chart_protein));
        set1.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.chart_protein));

        set2 = getDefaultLineDataSet(valuesFat, getString(R.string.macro_fat));
        set2.setColor(ContextCompat.getColor(requireActivity(), R.color.chart_fat));
        set2.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.chart_fat));

        set3 = getDefaultLineDataSet(valuesCarbo, getString(R.string.macro_carbo));
        set3.setColor(ContextCompat.getColor(requireActivity(), R.color.chart_carbo));
        set3.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.chart_carbo));

        LineData data = new LineData(set1, set2, set3);
        mBinding.chartMacro.setData(data);
    }

    private LineDataSet getDefaultLineDataSet(List<Entry> values, String label) {
        LineDataSet result = new LineDataSet(values, label);
        result.setLineWidth(2f);
        result.setCircleRadius(4f);
        result.setDrawCircleHole(false);
        result.setDrawCircles(false);
        result.setDrawValues(false);
        result.setDrawHorizontalHighlightIndicator(false);
        result.setHighLightColor(ContextCompat.getColor(requireActivity(), R.color.chart_label_color));
        return result;
    }

    private void generateWaterData(List<Water> waterList) {
        int amount;
        int axisIndex = 0;
        Calendar currentWeek = (Calendar) mFirstDayOfWeek.clone();

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsWater = new ArrayList<>();

        List<SubcolumnValue> valuesWater;

        for (Water water : waterList ) {
            amount = water.getAmount();
            valuesWater = new ArrayList<>();
            valuesWater.add(new SubcolumnValue(amount,
                    ContextCompat.getColor(requireActivity(), R.color.colorAccent)));
            axisValues.add(new AxisValue(axisIndex++).setLabel(mDateFormatter.format(currentWeek.getTime())));
            Column column = new Column(valuesWater);
            column.setHasLabels(hasLabels && amount != 0);
            columnsWater.add(column);

            currentWeek.add(Calendar.DAY_OF_WEEK, 1);
        }

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);

        ColumnChartData dataWater = new ColumnChartData(columnsWater);
        dataWater.setAxisXBottom(axisX);
        dataWater.setAxisYLeft(axisY);

        mBinding.chartWater.setColumnChartData(dataWater);
        mBinding.chartWater.setZoomEnabled(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        List<ILineDataSet> sets = mBinding.chartMacro.getData().getDataSets();

        sets.get(0).setVisible(mBinding.chProt.isChecked());
        sets.get(1).setVisible(mBinding.chFat.isChecked());
        sets.get(2).setVisible(mBinding.chCarbo.isChecked());

        mBinding.chartMacro.invalidate();
    }

    private class DaysValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Calendar currentWeek = (Calendar) mFirstDayOfWeek.clone();
            currentWeek.set(Calendar.DAY_OF_WEEK, (int) value + 2);

            return mDateFormatter.format(currentWeek.getTime());
        }
    }
}
