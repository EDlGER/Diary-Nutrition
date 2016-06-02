package ediger.diarynutrition.fragments.intro;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ediger.diarynutrition.R;


/**
 * Created by root on 12.05.16.
 */
public class FirstSlide extends Fragment {

    private static final String KEY_PREF_GENDER = "gender";
    private static final String KEY_PREF_BIRTHDAY = "birthday";
    private static final String KEY_PREF_HEIGHT = "height";

    private int genderId = 1;
    private int year = 1990;
    private int month = 0;
    private int day = 1;

    private SharedPreferences pref;
    Calendar calendar = Calendar.getInstance();
    private RadioGroup radioGender;
    private EditText txtBirthday;
    private EditText txtHeight;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_1, container, false);

        radioGender = (RadioGroup) view.findViewById(R.id.rgGender);
        radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioGender.findViewById(checkedId);
                genderId = radioGender.indexOfChild(checkedButton) + 1;
            }
        });

        txtBirthday = (EditText) view.findViewById(R.id.edBirthday);
        txtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), d, year, month, day).show();
            }
        });

        txtHeight = (EditText) view.findViewById(R.id.edHeight);
        txtHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtHeight.getText().toString().equals("")) {
                    txtHeight.setText("170");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreference();
    }

    public void savePreference() {
        calendar.set(year,month,day);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_GENDER,String.valueOf(genderId));
        editor.putString(KEY_PREF_HEIGHT,txtHeight.getText().toString());
        editor.putLong(KEY_PREF_BIRTHDAY,calendar.getTimeInMillis());

        editor.apply();

    }

    private DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            FirstSlide.this.year = year;
            month = monthOfYear;
            day = dayOfMonth;

            calendar.set(year,month,day);
            txtBirthday.setText(dateFormatter.format(calendar.getTime()));
        }
    };

}
