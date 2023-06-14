package ediger.diarynutrition.settings

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ediger.diarynutrition.DATABASE_NAME
import ediger.diarynutrition.KEY_ACTIVITY
import ediger.diarynutrition.KEY_BIRTHDAY
import ediger.diarynutrition.KEY_GENDER
import ediger.diarynutrition.KEY_HEIGHT
import ediger.diarynutrition.KEY_PREF_UI_DEFAULT_TAB
import ediger.diarynutrition.KEY_PROGRAM_CAL
import ediger.diarynutrition.KEY_PROGRAM_WATER
import ediger.diarynutrition.KEY_PURPOSE
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.intro.PolicyActivity
import ediger.diarynutrition.util.NutritionProgramUtils
import ediger.diarynutrition.util.SnackbarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    private val viewModel: SettingsViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    private val date = Date()

    // TODO: Get rid of this approach
    private var isBackupRequested = false
    private var isRestoreRequested = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        PreferenceHelper.getPreferences().registerOnSharedPreferenceChangeListener(this)

        findPreference(KEY_HEIGHT).summary =
            PreferenceHelper.getValue(KEY_HEIGHT, String::class.java, "")

        with(findPreference(KEY_BIRTHDAY)) {
            date.time = PreferenceHelper.getValue(KEY_BIRTHDAY, Long::class.javaObjectType, 0L)
            summary = dateFormat.format(date)
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    showDateDialog()
                    false
                }
        }

        with(findPreference(KEY_PROGRAM_CAL)) {
            val cal = PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float::class.javaObjectType, 0f)
            summary = cal.toInt().toString()
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    ChangeCaloriesDialog().show(parentFragmentManager, null)
                    false
                }
        }

        with(findPreference(KEY_PROGRAM_WATER)) {
            summary = PreferenceHelper.getValue(
                KEY_PROGRAM_WATER,
                Int::class.javaObjectType,
                NutritionProgramUtils.getDefaultWater()
            ).toString()
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    ChangeWaterDialog().show(parentFragmentManager, null)
                    false
                }
        }

        with(findPreference(KEY_PROGRAM_WATER)) {
            summary = PreferenceHelper.getValue(
                KEY_PROGRAM_WATER,
                Int::class.javaObjectType,
                NutritionProgramUtils.getDefaultWater()
            ).toString()
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    ChangeWaterDialog().show(parentFragmentManager, null)
                    false
                }
        }

        findPreference(KEY_PREF_PROGRAM_EDIT).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(requireActivity(), ProgramActivity::class.java))
                false
            }

        findPreference(KEY_PREF_PROGRAM_MEALS).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_setting_to_user_meal)
                false
            }
        findPreference(KEY_PREF_PROGRAM_RESET).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                NutritionProgramUtils.setToDefault()
                false
            }

        // TODO: Fix wrong path (Do I need it?)
        val pathData = findPreference(KEY_PREF_DATA_PATH)
        val path = Environment.getExternalStorageDirectory().path +
                "/DiaryNutrition/" + DATABASE_NAME
        pathData.summary = path


        findPreference(KEY_PREF_DATA_BACKUP).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                viewModel.backupDatabase()
                isBackupRequested = true
                false
            }

        findPreference(KEY_PREF_DATA_RESTORE).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.dialog_restore_title))
                    .setMessage(getString(R.string.dialog_restore_message))
                    .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> }
                    .setPositiveButton(getString(R.string.dialog_restore_positive)) { _, _ ->
                        viewModel.restoreDatabase()
                        isRestoreRequested = true
                    }
                    .show()
                false
            }

        findPreference(KEY_PREF_POLICY).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(requireActivity(), PolicyActivity::class.java))
                false
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.backupStatus.observe(viewLifecycleOwner) { listOfInfos: List<WorkInfo>? ->
            if (listOfInfos.isNullOrEmpty() || !isBackupRequested) {
                return@observe
            }
            val workInfo = listOfInfos[0]
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                SnackbarUtils.showSnackbar(view, getString(R.string.message_data_backup))
                isBackupRequested = false
            }
        }
        viewModel.restoreStatus.observe(viewLifecycleOwner) { listOfInfos: List<WorkInfo>? ->
            if (listOfInfos.isNullOrEmpty() || !isRestoreRequested) {
                return@observe
            }
            val workInfo = listOfInfos[0]
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                SnackbarUtils.showSnackbar(view, getString(R.string.message_data_restore))
                isRestoreRequested = false
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            KEY_GENDER, KEY_ACTIVITY, KEY_PURPOSE -> {
                findPreference(key).summary = "%s"
                NutritionProgramUtils.setToDefault()
            }
            KEY_PREF_UI_DEFAULT_TAB -> findPreference(key).summary = "%s"
            KEY_PREF_DATA_LANGUAGE -> {
                // TODO: Require changes
                val restartIntent = requireActivity().packageManager
                    .getLaunchIntentForPackage(requireActivity().packageName)
                val intent = PendingIntent.getActivity(
                    requireActivity(), 0,
                    restartIntent, PendingIntent.FLAG_CANCEL_CURRENT
                )
                val manager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                manager[AlarmManager.RTC, System.currentTimeMillis() + 10] = intent
                System.exit(2)
            }
            KEY_BIRTHDAY -> {
                date.time = sharedPreferences.getLong(key, 0L)
                findPreference(key).summary = dateFormat.format(date)
                NutritionProgramUtils.setToDefault()
            }
            KEY_HEIGHT -> {
                findPreference(key).summary = sharedPreferences.getString(key, "")
                NutritionProgramUtils.setToDefault()
            }
            KEY_PROGRAM_CAL -> {
                findPreference(key).summary = sharedPreferences.getFloat(key, 1f).toInt().toString()
                NutritionProgramUtils.update()
            }
            KEY_PROGRAM_WATER -> findPreference(key).summary =
                sharedPreferences.getInt(key, NutritionProgramUtils.getDefaultWater()).toString()
        }
    }

    private fun showDateDialog() {
        val birthday = Calendar.getInstance().apply {
            timeInMillis = PreferenceHelper.getValue(KEY_BIRTHDAY, Long::class.javaObjectType, 0L)
        }
        DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                birthday[year, monthOfYear] = dayOfMonth
                PreferenceHelper.setValue(KEY_BIRTHDAY, birthday.timeInMillis)
            },
            birthday[Calendar.YEAR],
            birthday[Calendar.MONTH],
            birthday[Calendar.DAY_OF_MONTH]
        ).show()
    }

    companion object {
        const val KEY_PREF_DATA_LANGUAGE = "data_language"
        const val KEY_PREF_DATA_PATH = "data_path"
        const val KEY_PREF_DATA_BACKUP = "data_backup"
        const val KEY_PREF_DATA_RESTORE = "data_restore"
        const val KEY_PREF_POLICY = "policy"
        private const val KEY_PREF_PROGRAM_EDIT = "program_edit"
        private const val KEY_PREF_PROGRAM_RESET = "program_reset"
        private const val KEY_PREF_PROGRAM_MEALS = "program_meals"
    }
}