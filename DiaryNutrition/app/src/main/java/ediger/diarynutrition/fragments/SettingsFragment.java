package ediger.diarynutrition.fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.preference.Preference;
//import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import java.util.Calendar;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;

/**
 * Created by Ediger on 03.05.2015.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.
        OnSharedPreferenceChangeListener, DatePickerDialog.OnDateSetListener{
    View rootview;

    public static final String KEY_PREF_BIRTHDAY = "birthday";
    public static final String KEY_PREF_ACTIVITY = "activity";
    public static final String KEY_PREF_PURPOSE = "purpose";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference birthdayPref = findPreference(KEY_PREF_BIRTHDAY);
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
        if (key.equals(KEY_PREF_ACTIVITY)) {
            Preference activityPref = findPreference(key);
            activityPref.setSummary(sharedPreferences.getString(key,""));
        }
        if (key.equals(KEY_PREF_PURPOSE)) {
            Preference purposePref = findPreference(key);
            purposePref.setSummary(sharedPreferences.getString(key,""));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }

    private void showDateDialog(){
        int year = 1990;
        int month = 1;
        int day = 1;
        new DatePickerDialog(getActivity(),this, year, month, day).show();

    }
}
