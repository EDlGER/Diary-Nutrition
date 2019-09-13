package ediger.diarynutrition.settings;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.NutritionProgramManager;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.DatabaseCopier;
import ediger.diarynutrition.intro.PolicyActivity;
import ediger.diarynutrition.AppContext;

import static ediger.diarynutrition.PreferenceHelper.*;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.
        OnSharedPreferenceChangeListener, DatePickerDialog.OnDateSetListener {

//    public static final String KEY_PREF_GENDER = "gender";
//    public static final String KEY_PREF_BIRTHDAY = "birthday";
//    public static final String KEY_PREF_HEIGHT = "height";
//    public static final String KEY_PREF_ACTIVITY = "activity";
//    public static final String KEY_PREF_PURPOSE = "purpose";
//    public static final String KEY_PREF_CALORIES = "calories";
//    public static final String KEY_PREF_WATER = "water";
    public static final String KEY_PREF_PROGRAM_EDIT = "program_edit";
    public static final String KEY_PREF_PROGRAM_RESET = "program_reset";
//    public static final String KEY_PREF_UI_WATER_CARD = "water_card";
    public static final String KEY_PREF_UI_DEFAULT_TAB = "default_tab";
    public static final String KEY_PREF_DATA_LANGUAGE= "data_language";
    public static final String KEY_PREF_DATA_PATH = "data_path";
    public static final String KEY_PREF_DATA_BACKUP = "data_backup";
    public static final String KEY_PREF_DATA_RESTORE = "data_restore";
    public static final String KEY_PREF_POLICY = "policy";

    private static final int REQ_WRITE_STORAGE = 112;

    private Calendar birthday = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    private Date date = new Date();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);

        PreferenceHelper.getPreferences().registerOnSharedPreferenceChangeListener(this);

        Preference heightPref = findPreference(KEY_HEIGHT);
        heightPref.setSummary(PreferenceHelper.getValue(KEY_HEIGHT, String.class, ""));

        Preference birthdayPref = findPreference(KEY_BIRTHDAY);
        date.setTime(PreferenceHelper.getValue(KEY_BIRTHDAY, Long.class, 0L));
        birthdayPref.setSummary(dateFormat.format(date));
        birthdayPref.setOnPreferenceClickListener(preference -> {
            showDateDialog();
            return false;
        });

        float cal = PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float.class, 0f);
        Preference caloriesPref = findPreference(KEY_PROGRAM_CAL);
        caloriesPref.setSummary(String.valueOf((int) cal));
        caloriesPref.setOnPreferenceClickListener(preference -> {
            new ChangeCaloriesDialog().show(requireFragmentManager(), null);
            return false;
        });

        Preference waterPref = findPreference(KEY_PROGRAM_WATER);
        waterPref.setSummary(String.valueOf(PreferenceHelper.getValue(
                KEY_PROGRAM_WATER,
                Integer.class,
                NutritionProgramManager.getInstance().getDefaultWater())));
        waterPref.setOnPreferenceClickListener(preference -> {
            new ChangeWaterDialog().show(requireFragmentManager(), null);
            return false;
        });

        Preference editProgram = findPreference(KEY_PREF_PROGRAM_EDIT);
        editProgram.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ProgramActivity.class));
            return false;
        });

        Preference resetProgram = findPreference(KEY_PREF_PROGRAM_RESET);
        resetProgram.setOnPreferenceClickListener(preference -> {
            NutritionProgramManager.getInstance().updateProgram();
            return false;
        });

        // TODO: fix wrong path
        Preference pathData = findPreference(KEY_PREF_DATA_PATH);
        String path = Environment.getExternalStorageDirectory().getPath() +
                "/DiaryNutrition/" +
                DatabaseCopier.getInstance(AppContext.getInstance()).getDatabaseName();
        pathData.setSummary(path);

        Preference backupData = findPreference(KEY_PREF_DATA_BACKUP);
        backupData.setOnPreferenceClickListener(preference -> {
            boolean hasPermission = (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_WRITE_STORAGE);
            }
            // TODO: backup db
            //AppContext.getDbDiary().backupDb();
            return false;
        });

        Preference restoreData = findPreference(KEY_PREF_DATA_RESTORE);
        restoreData.setOnPreferenceClickListener(preference -> {
            boolean hasPermission = (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_WRITE_STORAGE);
            }
            // TODO: restore db
            //AppContext.getDbDiary().restoreDb();
            return false;
        });

        Preference privacyPolicy = findPreference(KEY_PREF_POLICY);
        privacyPolicy.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), PolicyActivity.class));
            return false;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: check (finish?)
                requireActivity().finish();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(requireActivity(), "Now app can write", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), "The app not allowed to write to your storage",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_GENDER) ||
                key.equals(KEY_ACTIVITY) || key.equals(KEY_PURPOSE)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
            NutritionProgramManager.getInstance().updateProgram();
        }
        if (key.equals(KEY_PREF_UI_DEFAULT_TAB)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
        }
        if (key.equals(KEY_PREF_DATA_LANGUAGE)) {
            Intent restartIntent = requireActivity().getPackageManager()
                    .getLaunchIntentForPackage(requireActivity().getPackageName() );
            PendingIntent intent = PendingIntent.getActivity(
                    requireActivity(), 0,
                    restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager manager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, intent);
            System.exit(2);
        }
        if (key.equals(KEY_BIRTHDAY)) {
            Preference birthdayPref = findPreference(key);
            date.setTime(sharedPreferences.getLong(key, 0L));

            birthdayPref.setSummary(dateFormat.format(date));
            NutritionProgramManager.getInstance().updateProgram();
        }
        if (key.equals(KEY_HEIGHT)) {
            Preference heightPref = findPreference(key);
            heightPref.setSummary(sharedPreferences.getString(key, ""));
            NutritionProgramManager.getInstance().updateProgram();
        }

        if (key.equals(KEY_PROGRAM_CAL)) {
            Preference caloriesPref = findPreference(key);
            caloriesPref.setSummary(String.valueOf(sharedPreferences.getFloat(key, 1f)));
            calculateUserProgram();
        }

        if (key.equals(KEY_PROGRAM_WATER)) {
            Preference waterPref = findPreference(key);
            waterPref.setSummary(String.valueOf(sharedPreferences.getInt(
                    key,
                    NutritionProgramManager.getInstance().getDefaultWater())));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        birthday.set(year,monthOfYear,dayOfMonth);
        PreferenceHelper.setValue(KEY_BIRTHDAY, birthday.getTimeInMillis());
    }

    private void showDateDialog() {
        birthday.setTimeInMillis(PreferenceHelper.getValue(KEY_BIRTHDAY, Long.class, 0L));

        new DatePickerDialog(requireActivity(),this,
                birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH)).show();

    }

    // TODO: move from View
    private void calculateUserProgram() {
        int cal = PreferenceHelper.getValue(KEY_PROGRAM_CAL, Integer.class, 1);

        float prot = (float) NutritionProgramManager.getInstance().getProtPercent() / 100 * (float) cal / 4;
        float fat = (float) NutritionProgramManager.getInstance().getFatPercent() / 100 * (float) cal / 9;
        float carbo = (float) NutritionProgramManager.getInstance().getCarboPercent() / 100 * (float) cal /4;

        PreferenceHelper.setValue(KEY_PROGRAM_PROT, prot);
        PreferenceHelper.setValue(KEY_PROGRAM_FAT, fat);
        PreferenceHelper.setValue(KEY_PROGRAM_PROT, carbo);
    }

}
