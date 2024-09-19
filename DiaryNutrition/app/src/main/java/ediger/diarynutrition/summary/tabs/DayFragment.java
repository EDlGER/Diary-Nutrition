package ediger.diarynutrition.summary.tabs;


import android.animation.AnimatorInflater;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.databinding.FragmentSummaryDayBinding;
import ediger.diarynutrition.summary.SummaryViewModel;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;

import static ediger.diarynutrition.PreferenceHelper.*;

public class DayFragment extends Fragment {

    private FragmentSummaryDayBinding mBinding;

    private SummaryViewModel mViewModel;

    private Calendar day = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_summary_day, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);

        mViewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary.size() == 1) {
                mBinding.setSummary(summary.get(0));
                generateData(summary.get(0));
            }
        });

        mViewModel.getWater().observe(getViewLifecycleOwner(), water -> {
            if (water.size() == 1) {
                mBinding.txtSumWater.setText(String.valueOf(water.get(0).getAmount()));
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Summary macroPercent = new Summary(
                0,
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_PROT_PERCENT, Integer.class, 0),
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_FAT_PERCENT, Integer.class, 0),
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_CARBO_PERCENT, Integer.class, 0)
        );
        mBinding.setMacroPercent(macroPercent);

        Summary goal = new Summary(
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_CAL, Float.class, 0f),
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_PROT, Float.class, 0f),
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_FAT, Float.class, 0f),
                PreferenceHelper.getValue(Consts.KEY_PROGRAM_CARBO, Float.class, 0f)
        );
        mBinding.setGoal(goal);

        mBinding.txtPurWater.setText(
                String.valueOf(PreferenceHelper.getValue(Consts.KEY_PROGRAM_WATER, Integer.class, 0))
        );


        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
        mBinding.datePicker.setOnClickListener(v ->
                new DatePickerDialog(requireActivity(),
                        (aView, year, monthOfYear, dayOfMonth) -> {
                            day.set(year,monthOfYear,dayOfMonth);
                            mViewModel.setPeriod(day, 1);
                            if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                                    == day.get(Calendar.DAY_OF_YEAR)) {
                                mBinding.txtDateDay.setText(getString(R.string.diary_date_today));
                            } else {
                                mBinding.txtDateDay.setText(dateFormat.format(day.getTime()));
                            }
                        },
                        day.get(Calendar.YEAR),
                        day.get(Calendar.MONTH),
                        day.get(Calendar.DAY_OF_MONTH))
                        .show()
        );
        mBinding.txtDateDay.setText(getString(R.string.diary_date_today));
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setPeriod(day, 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) requireActivity()).appBar
                    .setStateListAnimator(AnimatorInflater
                            .loadStateListAnimator(getActivity(), R.animator.appbar_unelevated_animator)
                    );
        }
    }

    private void generateData(Summary summary) {

        if (summary == null) {
            return;
        }

        //float cal = summary.getCal();
        float prot = summary.getProt() * 4;
        float fat = summary.getFat() * 9;
        float carbo = summary.getCarbo() * 4;
        float cal = prot + fat + carbo;

        List<SliceValue> values = new ArrayList<>();


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
        if (carbo != 0) {
            SliceValue carboSliceValue = new SliceValue(carbo,ChartUtils.COLOR_ORANGE);
            carboSliceValue.setLabel((int) (carbo / cal * 100) + "%");
            values.add(carboSliceValue);
        }

        PieChartData data = new PieChartData(values);
        data.setHasLabels(true);
        mBinding.pieChart.setCircleFillRatio(1.0f);
        mBinding.pieChart.setPieChartData(data);
    }
}
