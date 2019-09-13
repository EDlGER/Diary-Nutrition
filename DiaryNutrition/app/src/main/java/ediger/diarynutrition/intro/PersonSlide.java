package ediger.diarynutrition.intro;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.FragmentIntroPersonBinding;
import ediger.diarynutrition.util.SnackbarUtils;
import static ediger.diarynutrition.PreferenceHelper.*;

import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.ISlideSelectionListener;


public class PersonSlide extends Fragment implements ISlidePolicy,ISlideSelectionListener {

    private FragmentIntroPersonBinding mBinding;

    private int genderId = 1;
    private int year = 1990;
    private int month = 0;
    private int day = 1;
    private Calendar calendar = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_person, container, false);

        mBinding.linkPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        mBinding.rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            AppCompatRadioButton checkedButton = mBinding.rgGender.findViewById(checkedId);
            genderId = mBinding.rgGender.indexOfChild(checkedButton) + 1;
        });

        calendar.set(year,month,day);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        mBinding.edBirthday.setText(dateFormatter.format(calendar.getTime()));
        mBinding.edBirthday.setOnClickListener(v ->
                new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
                    PersonSlide.this.year = year;
                    month = monthOfYear;
                    day = dayOfMonth;

                    calendar.set(year,month,day);
                    mBinding.edBirthday.setText(dateFormatter.format(calendar.getTime()));},
                year, month, day).show());

        return mBinding.getRoot();
    }


    @Override
    public boolean isPolicyRespected() {
        String height = mBinding.edHeight.getText().toString();
        return !height.equals("") && !height.equals("0");
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        SnackbarUtils.showSnackbar(mBinding.getRoot(),getString(R.string.intro_height_error));
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        savePreference();
    }

    private void savePreference() {
        calendar.set(year,month,day);

        PreferenceHelper.setValue(KEY_GENDER, String.valueOf(genderId));
        PreferenceHelper.setValue(KEY_BIRTHDAY, calendar.getTimeInMillis());
        PreferenceHelper.setValue(KEY_HEIGHT, mBinding.edHeight.getText().toString());
    }
}
