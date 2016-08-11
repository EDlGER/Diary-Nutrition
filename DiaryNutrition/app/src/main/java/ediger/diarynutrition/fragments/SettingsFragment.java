package ediger.diarynutrition.fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.github.machinarius.preferencefragment.PreferenceFragment;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

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

    private SharedPreferences pref;
    Date date = new Date();
    private Calendar birthday = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pref.registerOnSharedPreferenceChangeListener(this);

        Preference heightPref = findPreference(KEY_PREF_HEIGHT);
        heightPref.setSummary(pref.getString(KEY_PREF_HEIGHT,""));

        Preference birthdayPref = findPreference(KEY_PREF_BIRTHDAY);
        date.setTime(pref.getLong(KEY_PREF_BIRTHDAY,0));
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
            date.setTime(pref.getLong(KEY_PREF_BIRTHDAY,0));

            birthdayPref.setSummary(dateFormat.format(date));
        }
        if (key.equals(KEY_PREF_HEIGHT)) {
            Preference heightPref = findPreference(key);
            heightPref.setSummary(sharedPreferences.getString(key,""));
        }

        calculateProgram();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        SharedPreferences.Editor editor = pref.edit();

        birthday.set(year,monthOfYear,dayOfMonth);
        editor.putLong(KEY_PREF_BIRTHDAY,birthday.getTimeInMillis());
        editor.apply();

    }

    private void showDateDialog() {

        birthday.setTimeInMillis(pref.getLong(KEY_PREF_BIRTHDAY,0));

        new DatePickerDialog(getActivity(),this,
                birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void calculateProgram() {
        int cal;
        int prot;
        int fat;
        int carbo;
        int purposeId = Integer.parseInt(pref.getString("purpose", "1")) - 1;
        int [] loss = {30, 30, 40};
        int [] deduction = {20, 30, 50};
        int [] gain = {30, 20, 50};
        int [][] purpose = {loss, deduction, gain};
        Calendar calendar = Calendar.getInstance();

        float weight = pref.getFloat("weight", 0);;
        float height;
        float age;
        float [] gender = {5,-161};
        double [] activity = {1.2, 1.375, 1.55, 1.725};
        int [] dimension = {-400, 0, 400};
        Calendar birthday = Calendar.getInstance();

        //weight = pref.getFloat("weight", 0);

        Cursor cursor = AppContext.getDbDiary().getAllWeight();
        if (cursor.moveToFirst()) {
            weight = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT)));
        }

        height = Float.parseFloat(pref.getString("height","0"));

        birthday.setTimeInMillis(pref.getLong("birthday",0));
        age = calendar.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

        cal = (int) (10 * weight + 6.25 * height - 5 * age +
                gender[Integer.parseInt(pref.getString("gender", "1")) - 1]);
        cal *= activity[Integer.parseInt(pref.getString("activity", "1")) - 1];
        cal += dimension[Integer.parseInt(pref.getString("purpose", "1")) - 1];

        prot = (int) ((double) purpose[purposeId][0] / 100 * (double) cal / 4);
        fat = (int) ((double) purpose[purposeId][1] / 100 * (double) cal / 9);
        carbo = (int) ((double) purpose[purposeId][2] / 100 * (double) cal / 4);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("calories", cal);
        editor.putInt("prot", prot);
        editor.putInt("fat", fat);
        editor.putInt("carbo", carbo);
        editor.putInt("prot_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][0]);
        editor.putInt("fat_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][1]);
        editor.putInt("carbo_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][2]);
        editor.apply();
    }
}
