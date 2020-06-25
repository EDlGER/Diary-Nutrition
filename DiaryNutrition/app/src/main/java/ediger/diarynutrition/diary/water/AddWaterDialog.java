package ediger.diarynutrition.diary.water;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.databinding.DialogAddWaterBinding;
import ediger.diarynutrition.diary.DiaryViewModel;
import ediger.diarynutrition.util.SnackbarUtils;

import static ediger.diarynutrition.PreferenceHelper.*;

public class AddWaterDialog extends DialogFragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String ARG_DATE = "date";

    private DialogAddWaterBinding mBinding;
    private DiaryViewModel mViewModel;

    private boolean isAdsRemoved;

    private Calendar mCurrentDatetime = Calendar.getInstance();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());

    private InterstitialAd mInterstitialAd;

    private TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCurrentDatetime.set(mCurrentDatetime.get(Calendar.YEAR),
                    mCurrentDatetime.get(Calendar.MONTH),
                    mCurrentDatetime.get(Calendar.DAY_OF_MONTH),
                    hourOfDay, minute);
            mBinding.edTime.setText(timeFormatter.format(mCurrentDatetime.getTime()));
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_water, container, false);

        isAdsRemoved = PreferenceHelper.getValue(Consts.PREF_ADS_REMOVED, Boolean.class, false);

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_inter_water_id));

        if (!isAdsRemoved) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }

        mCurrentDatetime.setTimeInMillis(getArguments().getLong(ARG_DATE, 0));
        mCurrentDatetime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        mCurrentDatetime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.edWater.requestFocus();
        mBinding.setOnClickListener(this);
        mBinding.setOnLongClickListener(this);

        mBinding.edTime.setText(timeFormatter.format(mCurrentDatetime.getTime()));
        mBinding.edTime.setOnClickListener(v -> {
            hideKeyboard();
            new TimePickerDialog(getActivity(), t, mCurrentDatetime.get(Calendar.HOUR_OF_DAY),
                    mCurrentDatetime.get(Calendar.MINUTE), true).show();
        });

        mBinding.toolbar.inflateMenu(R.menu.menu_dialog_water);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            hideKeyboard();
            dismiss();
        });
        mBinding.toolbar.setOnMenuItemClickListener(item -> {
            addWater();
            return true;
        });

        //Toggle keyboard at start
        InputMethodManager im = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_water_1:
                mViewModel.addWater(new Water(Integer.parseInt(mBinding.txtWater1.getText().toString()),
                        mCurrentDatetime.getTimeInMillis()));
                hideKeyboard();

                if (!isAdsRemoved && mInterstitialAd.isLoaded()) mInterstitialAd.show();

                dismiss();
                break;
            case R.id.card_water_2:
                mViewModel.addWater(new Water(Integer.parseInt(mBinding.txtWater2.getText().toString()),
                        mCurrentDatetime.getTimeInMillis()));
                hideKeyboard();

                if (!isAdsRemoved && mInterstitialAd.isLoaded()) mInterstitialAd.show();

                dismiss();
                break;
            case R.id.card_water_3:
                mViewModel.addWater(new Water(Integer.parseInt(mBinding.txtWater3.getText().toString()),
                        mCurrentDatetime.getTimeInMillis()));
                hideKeyboard();

                if (!isAdsRemoved && mInterstitialAd.isLoaded()) mInterstitialAd.show();

                dismiss();
                break;
            case R.id.card_water_4:
                mViewModel.addWater(new Water(Integer.parseInt(mBinding.txtWater4.getText().toString()),
                        mCurrentDatetime.getTimeInMillis()));
                hideKeyboard();

                if (!isAdsRemoved && mInterstitialAd.isLoaded()) mInterstitialAd.show();

                dismiss();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.card_water_1:
                showDialog(0);
                break;
            case R.id.card_water_2:
                showDialog(1);
                break;
            case R.id.card_water_3:
                showDialog(2);
                break;
            case R.id.card_water_4:
                showDialog(3);
                break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        waterValuesInit();
    }

    private void addWater() {
        if (mBinding.edWater.getText().toString().matches("")
                || mBinding.edWater.getText().toString().startsWith("0")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.message_dialog_water),
                    Toast.LENGTH_LONG).show();
        } else {
            mViewModel.addWater(new Water(Integer.parseInt(mBinding.edWater.getText().toString()),
                    mCurrentDatetime.getTimeInMillis()));
            hideKeyboard();

            if (!isAdsRemoved && mInterstitialAd.isLoaded()) mInterstitialAd.show();

            dismiss();
        }

    }

    private void hideKeyboard() {
        View view = getView().findFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void waterValuesInit() {
        String[] consts = {
                Consts.PREF_WATER_SERVING_1,
                Consts.PREF_WATER_SERVING_2,
                Consts.PREF_WATER_SERVING_3,
                Consts.PREF_WATER_SERVING_4,
        };

        TextView[] txtViews = {mBinding.txtWater1, mBinding.txtWater2, mBinding.txtWater3, mBinding.txtWater4};

        for (int i = 0; i < consts.length; i++) {
            int value = PreferenceHelper.getValue(consts[i], Integer.class, 0);
            if (value != 0) {
                txtViews[i].setText(String.valueOf(value));
            }
        }
    }

    private void showDialog(int cardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
        View root = getActivity().getLayoutInflater().inflate(R.layout.dialog_water_serving, null);

        builder.setView(root);
        builder.setTitle(getString(R.string.dialog_title_water_serving));

        final TextInputEditText edWater = root.findViewById(R.id.ed_water_serving);
        edWater.requestFocus();

        builder.setPositiveButton(R.string.dialog_change, (dialog, which) -> {
            if (edWater.getText().toString().matches("") || edWater.getText().toString()
                    .startsWith("0")) {
                SnackbarUtils.showSnackbar(getView(), getString(R.string.message_dialog_water));
            } else {
                switch (cardId) {
                    case 0:
                        mBinding.txtWater1.setText(edWater.getText().toString());
                        PreferenceHelper.setValue(Consts.PREF_WATER_SERVING_1,
                                Integer.parseInt(edWater.getText().toString()));
                        break;
                    case 1:
                        mBinding.txtWater2.setText(edWater.getText().toString());
                        PreferenceHelper.setValue(Consts.PREF_WATER_SERVING_2,
                                Integer.parseInt(edWater.getText().toString()));
                        break;
                    case 2:
                        mBinding.txtWater3.setText(edWater.getText().toString());
                        PreferenceHelper.setValue(Consts.PREF_WATER_SERVING_3,
                                Integer.parseInt(edWater.getText().toString()));
                        break;
                    case 3:
                        mBinding.txtWater4.setText(edWater.getText().toString());
                        PreferenceHelper.setValue(Consts.PREF_WATER_SERVING_4,
                                Integer.parseInt(edWater.getText().toString()));
                        break;
                }
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss());
        builder.create();
        builder.show();
    }

}


