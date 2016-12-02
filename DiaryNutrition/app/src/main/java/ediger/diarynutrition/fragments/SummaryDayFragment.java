package ediger.diarynutrition.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

public class SummaryDayFragment extends Fragment {
    View rootview;

    private long date;

    SharedPreferences pref;

    private TextView txtCal;
    private TextView txtCarbo;
    private TextView txtProt;
    private TextView txtFat;

    private TextView txtCalRec;
    private TextView txtCarboRec;
    private TextView txtProtRec;
    private TextView txtFatRec;

    private TextView txtCarboRecPersent;
    private TextView txtProtRecPersent;
    private TextView txtFatRecPersent;

    private PieChartView chart;
    private PieChartView chartEmpty;
    private PieChartData data;
    private Calendar nowto;
    private Calendar today = Calendar.getInstance();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Calendar now = Calendar.getInstance();

        rootview = inflater.inflate(R.layout.fragment_summary_day,container,false);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        chart = (PieChartView) rootview.findViewById(R.id.pie_chart);
        chartEmpty = (PieChartView) rootview.findViewById(R.id.pie_chart_empty);

        txtCal = (TextView) rootview.findViewById(R.id.txt_sum_cal);
        txtCarbo = (TextView) rootview.findViewById(R.id.txt_sum_carbo);
        txtProt = (TextView) rootview.findViewById(R.id.txt_sum_prot);
        txtFat = (TextView) rootview.findViewById(R.id.txt_sum_fat);

        txtCalRec = (TextView) rootview.findViewById(R.id.txt_pur_cal);
        txtCarboRec = (TextView) rootview.findViewById(R.id.txt_pur_carbo);
        txtProtRec = (TextView) rootview.findViewById(R.id.txt_pur_prot);
        txtFatRec = (TextView) rootview.findViewById(R.id.txt_pur_fat);

        txtCarboRecPersent = (TextView) rootview.findViewById(R.id.txt_carbo_rec);
        txtProtRecPersent = (TextView) rootview.findViewById(R.id.txt_prot_rec);
        txtFatRecPersent = (TextView) rootview.findViewById(R.id.txt_fat_rec);

        txtCarboRecPersent.setText(String.valueOf(pref.getInt("carbo_pers", 0)) + "%");
        txtProtRecPersent.setText(String.valueOf(pref.getInt("prot_pers", 0)) + "%");
        txtFatRecPersent.setText(String.valueOf(pref.getInt("fat_pers", 0)) + "%");

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
        AppContext.getDbDiary().setDate(date);

        generateData();

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
                AppContext.getDbDiary().setDate(date);

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

    private void generateData() {

        Cursor cursor = AppContext.getDbDiary().getDayData(date);
        cursor.moveToFirst();

        txtCalRec.setText(String.valueOf(pref.getInt("calories", 0)));
        txtCarboRec.setText(String.valueOf(pref.getInt("carbo", 0)));
        txtProtRec.setText(String.valueOf(pref.getInt("prot", 0)));
        txtFatRec.setText(String.valueOf(pref.getInt("fat", 0)));

        txtCal.setText(String.format(Locale.getDefault(),"%.1f",
                cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL))));
        txtCarbo.setText(String.format(Locale.getDefault(),"%.1f",
                cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO))));
        txtProt.setText(String.format(Locale.getDefault(),"%.1f",
                cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT))));
        txtFat.setText(String.format(Locale.getDefault(),"%.1f",
                cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT))));

        float cal = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
        float carbo = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)) * 4;
        float prot = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT)) * 4;
        float fat = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT)) * 9;

        List<SliceValue> values = new ArrayList<>();

        if (carbo != 0) {
            SliceValue carboSliceValue = new SliceValue(carbo,ChartUtils.COLOR_ORANGE);
            carboSliceValue.setLabel((int) (carbo / cal * 100) + "%");
            values.add(carboSliceValue);
        }
        if (prot != 0) {
            SliceValue protSliceValue = new SliceValue(prot,ChartUtils.COLOR_GREEN);
            protSliceValue.setLabel((int) (prot / cal * 100) + "%");
            values.add(protSliceValue);
        }
        if (fat != 0) {
            SliceValue fatSliceValue = new SliceValue(fat,ChartUtils.COLOR_RED);
            fatSliceValue.setLabel((int) (fat / cal * 100) + "%");
            values.add(fatSliceValue);
        }

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

            txtCal.setText("0");
            txtCarbo.setText("0");
            txtProt.setText("0");
            txtFat.setText("0");
        }
        cursor.close();
    }
}
