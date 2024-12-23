package ediger.diarynutrition.settings

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.DEFAULT_LANGUAGE
import ediger.diarynutrition.KEY_ACTIVITY
import ediger.diarynutrition.KEY_BIRTHDAY
import ediger.diarynutrition.KEY_GENDER
import ediger.diarynutrition.KEY_HEIGHT
import ediger.diarynutrition.KEY_LANGUAGE
import ediger.diarynutrition.KEY_LANGUAGE_DB
import ediger.diarynutrition.KEY_PREF_UI_DEFAULT_TAB
import ediger.diarynutrition.KEY_PROGRAM_CAL
import ediger.diarynutrition.KEY_PROGRAM_WATER
import ediger.diarynutrition.KEY_PURPOSE
import ediger.diarynutrition.MainActivity
import ediger.diarynutrition.PreferenceHelper
import ediger.diarynutrition.R
import ediger.diarynutrition.diary.ChangeRecordDialog
import ediger.diarynutrition.intro.PolicyActivity
import ediger.diarynutrition.util.NutritionProgramUtils
import ediger.diarynutrition.util.setupSnackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    private val viewModel: SettingsViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    private val date = Date()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

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

        (findPreference(KEY_PREF_DATA_LANGUAGE) as ListPreference).value = PreferenceHelper
            .getValue(KEY_LANGUAGE_DB, String::class.java, DEFAULT_LANGUAGE)

        findPreference(KEY_PREF_DATA_BACKUP).onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showBackupDialog()
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
        view.setupSnackbar(viewLifecycleOwner, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)

        // If positive button of BackupDialog was pressed
        observeBackupRequest()

        viewModel.backupStatus.observe(viewLifecycleOwner) { listOfInfos: List<WorkInfo>? ->
            listOfInfos?.let { viewModel.concludeBackup(listOfInfos) }
        }
        viewModel.restoreStatus.observe(viewLifecycleOwner) { listOfInfos: List<WorkInfo>? ->
            listOfInfos?.let { viewModel.concludeRestore(listOfInfos) }
        }

        val toggleProgressAction = Observer<Boolean> { toggle ->
            (requireActivity() as MainActivity).toggleProgress(toggle)
        }
        viewModel.isBackupRequested.observe(viewLifecycleOwner, toggleProgressAction)
        viewModel.isRestoreRequested.observe(viewLifecycleOwner, toggleProgressAction)
        viewModel.isChangeLangRequested.observe(viewLifecycleOwner) {toggle ->
            toggleProgressAction.onChanged(toggle)
            if (toggle && viewModel.changeLangStatus?.hasObservers() == false) {
                viewModel.changeLangStatus?.observe(viewLifecycleOwner) {
                    viewModel.changeLangStateAltered(it)
                }
            } else if (!toggle) viewModel.changeLangStatus?.removeObservers(viewLifecycleOwner)
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceHelper.getPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceHelper.getPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            KEY_GENDER, KEY_ACTIVITY, KEY_PURPOSE -> {
                findPreference(key).summary = "%s"
                NutritionProgramUtils.setToDefault()
            }
            KEY_PREF_UI_DEFAULT_TAB -> findPreference(key).summary = "%s"
            KEY_LANGUAGE -> {
                sharedPreferences.getString(key, DEFAULT_LANGUAGE)?.let { language ->
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            }
            KEY_PREF_DATA_LANGUAGE -> {
                sharedPreferences.getString(key, DEFAULT_LANGUAGE)?.let { language ->
                    val oldLang = PreferenceHelper
                        .getValue(KEY_LANGUAGE_DB, String::class.java, DEFAULT_LANGUAGE)
                    if (!oldLang.equals(language)) viewModel.changeDbLanguage(language)
                }
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

    private fun observeBackupRequest() {
        val currentEntry = NavHostFragment.findNavController(this)
            .getBackStackEntry(R.id.nav_settings)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME
                && currentEntry.savedStateHandle.contains(BackupDialog.ARG_PERFORM_BACKUP)) {
                currentEntry.savedStateHandle.get<Boolean>(BackupDialog.ARG_PERFORM_BACKUP)?.let { performBackup ->
                    if (performBackup) viewModel.backupDatabase()
                    currentEntry.savedStateHandle.remove<Boolean>(BackupDialog.ARG_PERFORM_BACKUP)
                }
            }
        }
        currentEntry.lifecycle.addObserver(observer)
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                currentEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun showBackupDialog() {
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_settings_to_dialog_backup)
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
        private const val KEY_PREF_DATA_LANGUAGE = "data_language"
        private const val KEY_PREF_DATA_BACKUP = "data_backup"
        private const val KEY_PREF_DATA_RESTORE = "data_restore"
        private const val KEY_PREF_POLICY = "policy"
        private const val KEY_PREF_PROGRAM_EDIT = "program_edit"
        private const val KEY_PREF_PROGRAM_RESET = "program_reset"
        private const val KEY_PREF_PROGRAM_MEALS = "program_meals"
    }
}