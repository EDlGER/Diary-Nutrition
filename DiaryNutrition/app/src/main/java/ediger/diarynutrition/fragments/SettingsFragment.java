package ediger.diarynutrition.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.github.machinarius.preferencefragment.PreferenceFragment;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.activity.PolicyActivity;
import ediger.diarynutrition.activity.ProgramActivity;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.dialogs.ChangeCaloriesDialog;
import ediger.diarynutrition.fragments.dialogs.ChangeWaterDialog;
import ediger.diarynutrition.objects.AppContext;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.
        OnSharedPreferenceChangeListener, DatePickerDialog.OnDateSetListener{

    public static final String KEY_PREF_GENDER = "gender";
    public static final String KEY_PREF_BIRTHDAY = "birthday";
    public static final String KEY_PREF_HEIGHT = "height";
    public static final String KEY_PREF_ACTIVITY = "activity";
    public static final String KEY_PREF_PURPOSE = "purpose";
    public static final String KEY_PREF_CALORIES = "calories";
    public static final String KEY_PREF_WATER = "water";
    public static final String KEY_PREF_PROGRAM_EDIT = "program_edit";
    public static final String KEY_PREF_PROGRAM_RESET = "program_reset";
    public static final String KEY_PREF_UI_WATER_CARD = "water_card";
    public static final String KEY_PREF_UI_DEFAULT_TAB = "default_tab";
    public static final String KEY_PREF_DATA_LANGUAGE= "data_language";
    public static final String KEY_PREF_DATA_PATH = "data_path";
    public static final String KEY_PREF_DATA_BACKUP = "data_backup";
    public static final String KEY_PREF_DATA_RESTORE = "data_restore";
    public static final String KEY_PREF_POLICY = "policy";

    private static final int REQ_WRITE_STORAGE = 112;

    private SharedPreferences pref;
    private Calendar birthday = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    Date date = new Date();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        pref.registerOnSharedPreferenceChangeListener(this);

        Preference heightPref = findPreference(KEY_PREF_HEIGHT);
        heightPref.setSummary(pref.getString(KEY_PREF_HEIGHT, ""));

        Preference birthdayPref = findPreference(KEY_PREF_BIRTHDAY);
        date.setTime(pref.getLong(KEY_PREF_BIRTHDAY, 0));
        birthdayPref.setSummary(dateFormat.format(date));
        birthdayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDateDialog();
                return false;
            }
        });

        Preference caloriesPref = findPreference(KEY_PREF_CALORIES);
        caloriesPref.setSummary(String.valueOf(pref.getInt(KEY_PREF_CALORIES, 0)));
        caloriesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialog = new ChangeCaloriesDialog();
                dialog.show(getFragmentManager(), "change_calories_dialog");
                return false;
            }
        });



        Preference waterPref = findPreference(KEY_PREF_WATER);
        waterPref.setSummary(String.valueOf(pref.getInt(KEY_PREF_WATER, getDefaultWater())));
        waterPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialog = new ChangeWaterDialog();
                dialog.show(getFragmentManager(), "change_water_dialog");
                return false;
            }
        });

        Preference editProgram = findPreference(KEY_PREF_PROGRAM_EDIT);
        editProgram.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ProgramActivity.class);
                startActivity(intent);
                return false;
            }
        });

        Preference resetProgram = findPreference(KEY_PREF_PROGRAM_RESET);
        resetProgram.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                calculateDefaultProgram();
                return false;
            }
        });

        Preference pathData = findPreference(KEY_PREF_DATA_PATH);
        String path = Environment.getExternalStorageDirectory().getPath()
                + "/DiaryNutrition/" + AppContext.getDbDiary().getDbName();
        pathData.setSummary(path);


        Preference backupData = findPreference(KEY_PREF_DATA_BACKUP);
        backupData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

                if (!hasPermission) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQ_WRITE_STORAGE);
                }
                AppContext.getDbDiary().backupDb();
                return false;
            }
        });

        Preference restoreData = findPreference(KEY_PREF_DATA_RESTORE);
        restoreData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

                if (!hasPermission) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQ_WRITE_STORAGE);
                }
                AppContext.getDbDiary().restoreDb();
                return false;
            }
        });

        Preference privacyPolicy = findPreference(KEY_PREF_POLICY);
        privacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), PolicyActivity.class);
                startActivity(intent);
                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getActivity().finish();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Now app can write", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "The app not allowed to write to your storage",
                            Toast.LENGTH_SHORT).show();
                }
        }
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

        if (getResources().getDisplayMetrics().density > 2.0) {
            mainActivity.title.setPadding(0, 40, 0, 0);
        } else {
            mainActivity.title.setPadding(0, 25, 0, 0);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_GENDER) ||
                key.equals(KEY_PREF_ACTIVITY) || key.equals(KEY_PREF_PURPOSE)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
            calculateDefaultProgram();
        }
        if (key.equals(KEY_PREF_UI_DEFAULT_TAB)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
        }
        if (key.equals(KEY_PREF_DATA_LANGUAGE)) {
            Intent restartIntent = getActivity().getPackageManager()
                    .getLaunchIntentForPackage(getActivity().getPackageName() );
            PendingIntent intent = PendingIntent.getActivity(
                    getActivity(), 0,
                    restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
            System.exit(2);
        }
        if (key.equals(KEY_PREF_BIRTHDAY)) {
            Preference birthdayPref = findPreference(key);
            date.setTime(pref.getLong(KEY_PREF_BIRTHDAY, 0));

            birthdayPref.setSummary(dateFormat.format(date));
            calculateDefaultProgram();
        }
        if (key.equals(KEY_PREF_HEIGHT)) {
            Preference heightPref = findPreference(key);
            heightPref.setSummary(sharedPreferences.getString(key, ""));
            calculateDefaultProgram();
        }

        if (key.equals(KEY_PREF_CALORIES)) {
            Preference caloriesPref = findPreference(key);
            caloriesPref.setSummary(String.valueOf(sharedPreferences.getInt(key, 0)));
            calculateUserProgram();
        }

        if (key.equals(KEY_PREF_WATER)) {
            Preference waterPref = findPreference(key);
            waterPref.setSummary(String.valueOf(sharedPreferences.getInt(key, getDefaultWater())));
        }
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

    private void calculateDefaultProgram() {
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

    private void calculateUserProgram() {
        int cal = pref.getInt(KEY_PREF_CALORIES, 0);
        float prot;
        float fat;
        float carbo;

        prot = (float) pref.getInt("prot_pers", 1) / 100 * (float) cal / 4;
        fat = (float) pref.getInt("fat_pers", 1) / 100 * (float) cal / 9;
        carbo = (float) pref.getInt("carbo_pers", 1) / 100 * (float) cal /4;

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("prot", (int) prot);
        editor.putInt("fat", (int) fat);
        editor.putInt("carbo", (int) carbo);
        editor.apply();
    }

    private int getDefaultWater() {
        int gender = Integer.parseInt(pref.getString(KEY_PREF_GENDER, "1"));
        float weight = 70;
        int water;

        Cursor cursor = AppContext.getDbDiary().getAllWeight();
        if (cursor.moveToFirst()) {
            weight = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT));
        }
        if (gender == 1) {
            water = (int) (weight * 35);
        } else {
            water = (int) (weight * 31);
        }

        return water;
    }
}
