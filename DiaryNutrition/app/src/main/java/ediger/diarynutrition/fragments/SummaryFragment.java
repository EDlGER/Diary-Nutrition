package ediger.diarynutrition.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;
import lecho.lib.hellocharts.listener.DummyVieportChangeListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by Ediger on 03.05.2015.
 */
public class SummaryFragment extends Fragment {
    View rootview;

    private long date;
    private PieChartView chart;
    private PieChartView chartEmpty;
    private PieChartData data;
    private Calendar nowto;
    private Calendar today = Calendar.getInstance();

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

        final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

        if (!getUserVisibleHint()) {
            return;
        }

        final MainActivity mainActivity = (MainActivity)getActivity();

        mainActivity.menuMultipleActions.setVisibility(View.INVISIBLE);

        mainActivity.datePicker.setVisibility(View.VISIBLE);

        mainActivity.mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {

            @Override
            public void onDayClick(Date dateClicked) {
                nowto.setTime(dateClicked);
                date = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(date);

                if (today.equals(nowto)) {
                    mainActivity.setSubtitle(getString(R.string.diary_date_today));
                } else {
                    mainActivity.setSubtitle(dateFormat.format(dateClicked));
                }

                generateData();

                mainActivity.hideCalendarView();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mainActivity.setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });

        mainActivity.title.setPadding(0, 0, 0, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Calendar now = Calendar.getInstance();

        rootview = inflater.inflate(R.layout.fragment_summary,container,false);

        chart = (PieChartView) rootview.findViewById(R.id.pie_chart);
        chartEmpty = (PieChartView) rootview.findViewById(R.id.pie_chart_empty);

        //Set current date
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND,0);
        today.set(Calendar.MILLISECOND,0);
        nowto = Calendar.getInstance();
        nowto.clear(Calendar.MILLISECOND);
        nowto.clear(Calendar.SECOND);
        nowto.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), 0, 0);
        date = nowto.getTimeInMillis();
        AppContext.getDbDiary().editDate(date);


        generateData();

        return rootview;
    }

    private void generateData() {

        Cursor cursor = AppContext.getDbDiary().getDayData(date);
        cursor.moveToFirst();

        float cal = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
        float carbo = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)) * 4;
        float prot = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT)) * 4;
        float fat = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT)) * 9;

        List<SliceValue> values = new ArrayList<>();

        SliceValue carboSliceValue = new SliceValue(carbo,ChartUtils.COLOR_RED);
        carboSliceValue.setLabel((int) (carbo / cal * 100) + "%");
        SliceValue protSliceValue = new SliceValue(prot,ChartUtils.COLOR_GREEN);
        protSliceValue.setLabel((int) (prot / cal * 100) + "%");
        SliceValue fatSliceValue = new SliceValue(fat,ChartUtils.COLOR_ORANGE);
        fatSliceValue.setLabel((int) (fat / cal * 100) + "%");

        values.add(carboSliceValue);
        values.add(protSliceValue);
        values.add(fatSliceValue);

        data = new PieChartData(values);
        data.setHasLabels(true);
        chart.setCircleFillRatio(1.0f);

        if (cal != 0) {
            chart.setPieChartData(data);
            chart.setVisibility(View.VISIBLE);
            chartEmpty.setVisibility(View.INVISIBLE);
        } else {
            chart.setVisibility(View.GONE);
            chartEmpty.setVisibility(View.VISIBLE);
        }
        cursor.close();
    }
}
