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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.WorkInfo;

import com.google.common.util.concurrent.ListenableFuture;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.intro.PolicyActivity;
import ediger.diarynutrition.util.NutritionProgramUtils;
import ediger.diarynutrition.util.SnackbarUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.
        OnSharedPreferenceChangeListener, DatePickerDialog.OnDateSetListener {

    public static final String KEY_PREF_DATA_LANGUAGE= "data_language";
    public static final String KEY_PREF_DATA_PATH = "data_path";
    public static final String KEY_PREF_DATA_BACKUP = "data_backup";
    public static final String KEY_PREF_DATA_RESTORE = "data_restore";
    public static final String KEY_PREF_POLICY = "policy";

    private static final String KEY_PREF_PROGRAM_EDIT = "program_edit";
    private static final String KEY_PREF_PROGRAM_RESET = "program_reset";
    private static final String KEY_PREF_PROGRAM_MEALS = "program_meals";

    private static final int REQ_WRITE_STORAGE = 112;

    private SettingsViewModel viewModel;

    private Calendar birthday = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    private Date date = new Date();

    // TODO: Get rid of this approach
    private boolean isBackupRequested = false;
    private boolean isRestoreRequested = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);

        PreferenceHelper.getPreferences().registerOnSharedPreferenceChangeListener(this);

        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        Preference heightPref = findPreference(Consts.KEY_HEIGHT);
        heightPref.setSummary(PreferenceHelper.getValue(Consts.KEY_HEIGHT, String.class, ""));

        Preference birthdayPref = findPreference(Consts.KEY_BIRTHDAY);
        date.setTime(PreferenceHelper.getValue(Consts.KEY_BIRTHDAY, Long.class, 0L));
        birthdayPref.setSummary(dateFormat.format(date));
        birthdayPref.setOnPreferenceClickListener(preference -> {
            showDateDialog();
            return false;
        });

        float cal = PreferenceHelper.getValue(Consts.KEY_PROGRAM_CAL, Float.class, 0f);
        Preference caloriesPref = findPreference(Consts.KEY_PROGRAM_CAL);
        caloriesPref.setSummary(String.valueOf((int) cal));
        caloriesPref.setOnPreferenceClickListener(preference -> {
            new ChangeCaloriesDialog().show(getParentFragmentManager(), null);
            return false;
        });

        Preference waterPref = findPreference(Consts.KEY_PROGRAM_WATER);
        waterPref.setSummary(String.valueOf(PreferenceHelper.getValue(
                Consts.KEY_PROGRAM_WATER,
                Integer.class,
                NutritionProgramUtils.getDefaultWater()
        )));
        waterPref.setOnPreferenceClickListener(preference -> {
            new ChangeWaterDialog().show(getParentFragmentManager(), null);
            return false;
        });

        Preference editProgram = findPreference(KEY_PREF_PROGRAM_EDIT);
        editProgram.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ProgramActivity.class));
            return false;
        });

        findPreference(KEY_PREF_PROGRAM_MEALS).setOnPreferenceClickListener(preference -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_setting_to_user_meal);
            return false;
        });

        Preference resetProgram = findPreference(KEY_PREF_PROGRAM_RESET);
        resetProgram.setOnPreferenceClickListener(preference -> {
            NutritionProgramUtils.setToDefault();
            return false;
        });

        // TODO: Fix wrong path (Do I need it?)
        Preference pathData = findPreference(KEY_PREF_DATA_PATH);
        String path = Environment.getExternalStorageDirectory().getPath() +
                "/DiaryNutrition/" + Consts.DATABASE_NAME;
        pathData.setSummary(path);

        Preference backupData = findPreference(KEY_PREF_DATA_BACKUP);
        backupData.setOnPreferenceClickListener(preference -> {
            viewModel.backupDatabase();
            isBackupRequested = true;
            return false;
        });

        Preference restoreData = findPreference(KEY_PREF_DATA_RESTORE);
        restoreData.setOnPreferenceClickListener(preference -> {
            viewModel.restoreDatabase();
            isRestoreRequested = true;
            return false;
        });

        Preference privacyPolicy = findPreference(KEY_PREF_POLICY);
        privacyPolicy.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), PolicyActivity.class));
            return false;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getBackupStatus().observe(getViewLifecycleOwner(), listOfInfos -> {
            if (listOfInfos == null || listOfInfos.isEmpty() || !isBackupRequested) {
                return;
            }
            WorkInfo workInfo = listOfInfos.get(0);
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                SnackbarUtils.showSnackbar(view, getString(R.string.message_data_backup));
                isBackupRequested = false;
            }
        });

        viewModel.getRestoreStatus().observe(getViewLifecycleOwner(), listOfInfos -> {
            if (listOfInfos == null || listOfInfos.isEmpty() || !isRestoreRequested) {
                return;
            }
            WorkInfo workInfo = listOfInfos.get(0);
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                SnackbarUtils.showSnackbar(view, getString(R.string.message_data_restore));
                isRestoreRequested = false;
            }
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
                Toast.makeText(requireActivity(), "The app is not allowed to write to your storage",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Consts.KEY_GENDER) ||
                key.equals(Consts.KEY_ACTIVITY) || key.equals(Consts.KEY_PURPOSE)) {
            Preference listPref = findPreference(key);
            listPref.setSummary("%s");
            NutritionProgramUtils.setToDefault();
        }
        if (key.equals(Consts.KEY_PREF_UI_DEFAULT_TAB)) {
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
        if (key.equals(Consts.KEY_BIRTHDAY)) {
            Preference birthdayPref = findPreference(key);
            date.setTime(sharedPreferences.getLong(key, 0L));

            birthdayPref.setSummary(dateFormat.format(date));
            NutritionProgramUtils.setToDefault();
        }
        if (key.equals(Consts.KEY_HEIGHT)) {
            Preference heightPref = findPreference(key);
            heightPref.setSummary(sharedPreferences.getString(key, ""));
            NutritionProgramUtils.setToDefault();
        }

        if (key.equals(Consts.KEY_PROGRAM_CAL)) {
            Preference caloriesPref = findPreference(key);
            caloriesPref.setSummary(
                    String.valueOf((int) sharedPreferences.getFloat(key, 1f))
            );
            NutritionProgramUtils.update();
        }

        if (key.equals(Consts.KEY_PROGRAM_WATER)) {
            Preference waterPref = findPreference(key);
            waterPref.setSummary(
                    String.valueOf(sharedPreferences.getInt(key, NutritionProgramUtils.getDefaultWater()))
            );
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        birthday.set(year,monthOfYear,dayOfMonth);
        PreferenceHelper.setValue(Consts.KEY_BIRTHDAY, birthday.getTimeInMillis());
    }

    private void showDateDialog() {
        birthday.setTimeInMillis(PreferenceHelper.getValue(Consts.KEY_BIRTHDAY, Long.class, 0L));

        new DatePickerDialog(requireActivity(),this,
                birthday.get(Calendar.YEAR),
                birthday.get(Calendar.MONTH),
                birthday.get(Calendar.DAY_OF_MONTH)).show();

    }
}
