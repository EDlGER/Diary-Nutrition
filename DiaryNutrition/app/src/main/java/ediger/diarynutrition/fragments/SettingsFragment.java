package ediger.diarynutrition.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
//import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.github.machinarius.preferencefragment.PreferenceFragment;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;

/**
 * Created by Ediger on 03.05.2015.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.
        OnSharedPreferenceChangeListener, DatePickerDialog.OnDateSetListener{

    public static final String KEY_PREF_GENDER = "gender";
    public static final String KEY_PREF_BIRTHDAY = "birthday";
    public static final String KEY_PREF_HEIGHT = "height";
    public static final String KEY_PREF_ACTIVITY = "activity";
    public static final String KEY_PREF_PURPOSE = "purpose";

    private SharedPreferences sPref;
    Date date = new Date();
    private Calendar birthday = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sPref.registerOnSharedPreferenceChangeListener(this);

        Preference heightPref = findPreference(KEY_PREF_HEIGHT);
        heightPref.setSummary(sPref.getString(KEY_PREF_HEIGHT,""));

        Preference birthdayPref = findPreference(KEY_PREF_BIRTHDAY);
        date.setTime(sPref.getLong(KEY_PREF_BIRTHDAY,0));
        birthdayPref.setSummary(dateFormat.format(date));

        birthdayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDateDialog();
                return false;
            }
        });
    }


    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!getUserVisibleHint()) {
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.menuMultipleActions.setVisibility(View.INVISIBLE);

        mainActivity.datePicker.setVisibility(View.INVISIBLE);
        mainActivity.title.setPadding(0, 25, 0, 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_GENDER) ||
                key.equals(KEY_PREF_ACTIVITY) || key.equals(KEY_PREF_PURPOSE)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
        }
        if (key.equals(KEY_PREF_BIRTHDAY)) {
            Preference birthdayPref = findPreference(key);
            date.setTime(sPref.getLong(KEY_PREF_BIRTHDAY,0));

            birthdayPref.setSummary(dateFormat.format(date));
        }
        if (key.equals(KEY_PREF_HEIGHT)) {
            Preference heightPref = findPreference(key);
            heightPref.setSummary(sharedPreferences.getString(key,""));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        SharedPreferences.Editor editor = sPref.edit();

        birthday.set(year,monthOfYear,dayOfMonth);
        editor.putLong(KEY_PREF_BIRTHDAY,birthday.getTimeInMillis());
        editor.apply();

    }

    private void showDateDialog() {

        birthday.setTimeInMillis(sPref.getLong(KEY_PREF_BIRTHDAY,0));

        new DatePickerDialog(getActivity(),this,
                birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH)).show();

    }
}
