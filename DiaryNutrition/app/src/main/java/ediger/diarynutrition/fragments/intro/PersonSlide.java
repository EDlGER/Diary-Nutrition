package ediger.diarynutrition.fragments.intro;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.R;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.ISlideSelectionListener;


public class PersonSlide extends Fragment implements ISlidePolicy,ISlideSelectionListener {

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
        View view = inflater.inflate(R.layout.fragment_intro_person, container, false);

        TextView linkPolicy = (TextView) view.findViewById(R.id.link_policy);
        linkPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        radioGender = (RadioGroup) view.findViewById(R.id.rg_gender);
        radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioGender.findViewById(checkedId);
                genderId = radioGender.indexOfChild(checkedButton) + 1;
            }
        });

        calendar.set(year,month,day);

        txtBirthday = (EditText) view.findViewById(R.id.ed_birthday);
        txtBirthday.setText(dateFormatter.format(calendar.getTime()));
        txtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), d, year, month, day).show();
            }
        });

        txtHeight = (EditText) view.findViewById(R.id.ed_height);
        txtHeight.setText("170");

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        return view;
    }

    private void savePreference() {
        calendar.set(year,month,day);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_GENDER,String.valueOf(genderId));
        editor.putLong(KEY_PREF_BIRTHDAY,calendar.getTimeInMillis());
        editor.putString(KEY_PREF_HEIGHT,txtHeight.getText().toString());

        editor.apply();

    }

    private DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            PersonSlide.this.year = year;
            month = monthOfYear;
            day = dayOfMonth;

            calendar.set(year,month,day);
            txtBirthday.setText(dateFormatter.format(calendar.getTime()));
        }
    };

    @Override
    public boolean isPolicyRespected() {
        if (txtHeight.getText().toString().equals("") || txtHeight.getText().toString().equals("0")) {
            return false;
        }
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        Toast.makeText(getContext(), R.string.intro_height_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        savePreference();
    }
}
