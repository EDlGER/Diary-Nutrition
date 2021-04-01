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
import ediger.diarynutrition.databinding.FragmentSummaryMonthBinding;
import ediger.diarynutrition.summary.ChartMarkerView;
import ediger.diarynutrition.summary.SummaryViewModel;
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;

public class MonthFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private FragmentSummaryMonthBinding mBinding;

    private SummaryViewModel mViewModel;

    private Calendar mFirstDayOfMonth = Calendar.getInstance();

    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("d", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_summary_month, container, false);

        mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SummaryViewModel.class);
        mViewModel.getSummary().observe(this, summary -> {
            if (summary.size() == mFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                generateCalData(summary);
                generateMacroData(summary);
                mBinding.chartMacro.invalidate();
            }
        });
        mViewModel.getWater().observe(this, water -> {
            if (water.size() == mFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {
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

        final Calendar currentMonth = (Calendar) mFirstDayOfMonth.clone();

        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(requireActivity(), R.array.months,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBinding.spMonthChange.setAdapter(adapter);
        mBinding.spMonthChange.setSelection(mFirstDayOfMonth.get(Calendar.MONTH));
        mBinding.spMonthChange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > currentMonth.get(Calendar.MONTH)) {
                    mFirstDayOfMonth.add(Calendar.YEAR, -1);
                }
                mFirstDayOfMonth.set(Calendar.MONTH, position);
                mViewModel.setPeriod(mFirstDayOfMonth, mFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chartInit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setPeriod(mFirstDayOfMonth, mFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) Objects.requireNonNull(getActivity())).appBar
                    .setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getActivity(), R.animator.appbar_unelevated_animator)
                    );
        }
    }

    private void chartInit() {
        IAxisValueFormatter formatter = new DaysValueFormatter();

        XAxis axisX = mBinding.chartMacro.getXAxis();
        axisX.setDrawGridLines(false);
        axisX.setAxisMaximum(31);
        axisX.setAxisMinimum(-1);
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
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsCal = new ArrayList<>();

        List<SubcolumnValue> valuesCal;

        Calendar chosenMonth = (Calendar) mFirstDayOfMonth.clone();
        int i = 0;
        for (Summary summary : summaryList) {
            valuesCal = new ArrayList<>();
            valuesCal.add(new SubcolumnValue(summary.getCal(),
                    ContextCompat.getColor(requireActivity(), R.color.colorAccent)));
            if (i % 5 == 0) {
                axisValues.add(new AxisValue(i).setLabel(mDateFormatter.format(chosenMonth.getTime())));
            }

            Column column = new Column(valuesCal);

            if (i == 0) {
                column.setFormatter(new SimpleColumnChartValueFormatter()
                        .setPrependedText("       ".toCharArray()));
            } else if (i == summaryList.size() - 1) {
                column.setFormatter(new SimpleColumnChartValueFormatter()
                        .setAppendedText("       ".toCharArray()));
            }

            column.setHasLabelsOnlyForSelected(true);
            columnsCal.add(column);
            chosenMonth.add(Calendar.DAY_OF_MONTH, 1);
            i++;
        }

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);
        axisY.setTextSize(10);

        ColumnChartData dataCal = new ColumnChartData(columnsCal);
        dataCal.setAxisXBottom(axisX);
        dataCal.setAxisYLeft(axisY);

        mBinding.chartCal.setValueSelectionEnabled(true);
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
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        List<AxisValue> axisValues = new ArrayList<>();

        List<Column> columnsWater = new ArrayList<>();

        List<SubcolumnValue> valuesWater;

        Calendar chosenMonth = (Calendar) mFirstDayOfMonth.clone();
        int i = 0;
        for (Water water : waterList) {
            valuesWater = new ArrayList<>();
            valuesWater.add(new SubcolumnValue(water.getAmount(),
                    ContextCompat.getColor(requireActivity(), R.color.colorAccent)));
            if (i % 5 == 0) {
                axisValues.add(new AxisValue(i).setLabel(mDateFormatter.format(chosenMonth.getTime())));
            }

            Column column = new Column(valuesWater);

            if (i == 0) {
                column.setFormatter(new SimpleColumnChartValueFormatter()
                        .setPrependedText("       ".toCharArray()));
            } else if (i == waterList.size() - 1) {
                column.setFormatter(new SimpleColumnChartValueFormatter()
                        .setAppendedText("       ".toCharArray()));
            }

            column.setHasLabelsOnlyForSelected(true);
            columnsWater.add(column);
            chosenMonth.add(Calendar.DAY_OF_MONTH, 1);
            i++;
        }

        axisX.setValues(axisValues);
        axisY.setMaxLabelChars(4);
        axisY.setTextSize(10);

        ColumnChartData dataWater = new ColumnChartData(columnsWater);
        dataWater.setAxisXBottom(axisX);
        dataWater.setAxisYLeft(axisY);

        mBinding.chartWater.setValueSelectionEnabled(true);
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
            Calendar chosenMonth = (Calendar) mFirstDayOfMonth.clone();
            chosenMonth.set(Calendar.DAY_OF_MONTH, (int) value + 1);

            if (chosenMonth.getActualMaximum(Calendar.MONTH) == 30 && value == 30) {
                return null;
            }

            if (value % 5 == 0) return mDateFormatter.format(chosenMonth.getTime());

            return null;
        }
    }
}
