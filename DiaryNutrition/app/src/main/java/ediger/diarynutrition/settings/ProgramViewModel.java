package ediger.diarynutrition.settings;

import android.app.Application;

import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static ediger.diarynutrition.PreferenceHelper.*;
import static ediger.diarynutrition.data.source.model.ProgramElement.*;

import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.model.Program;

public class ProgramViewModel extends AndroidViewModel {

    public MutableLiveData<Boolean> isPercentSelected = new MutableLiveData<>(true);

    public LiveData<Program> program;

    public ProgramViewModel(@NonNull Application application) {
        super(application);

        program = new MutableLiveData<>(new Program(
                PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float.class, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_PROT, Float.class, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_FAT, Float.class, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_CARBO, Float.class, 1f),
                PreferenceHelper.getValue(KEY_PROGRAM_PROT_PERCENT, Integer.class, 1),
                PreferenceHelper.getValue(KEY_PROGRAM_FAT_PERCENT, Integer.class, 1),
                PreferenceHelper.getValue(KEY_PROGRAM_CARBO_PERCENT, Integer.class, 1)
        ));
    }

    public void onCaloriesChanged(CharSequence text) {
        if (program.getValue() == null
                || text == null
                || text.length() == 0
                || (isPercentSelected.getValue() != null && !isPercentSelected.getValue())) {
            return;
        }
        program.getValue().setProgramElement(CALORIES, Float.parseFloat(text.toString()));
    }

    public void onSpinnerItemSelected(int position) {
        isPercentSelected.setValue(position == 0);
    }

    public void onSeekProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (program.getValue() == null || isPercentSelected.getValue() == null) return;

        if (isPercentSelected.getValue()) {
            progress = (Math.round(progress / 5f)) * 5;
            seekBar.setProgress(progress);

            switch (seekBar.getId()) {
                case R.id.sb_prot_percent:
                    program.getValue().setProgramElement(PROT_PCT, progress);
                    break;
                case R.id.sb_fat_percent:
                    program.getValue().setProgramElement(FAT_PCT, progress);
                    break;
                case R.id.sb_carbo_percent:

                    program.getValue().setProgramElement(CARBO_PCT, progress);
                    break;
            }
        } else {
            switch (seekBar.getId()) {
                case R.id.sb_prot_gram:
                    program.getValue().setProgramElement(PROT, progress);
                    break;
                case R.id.sb_fat_gram:
                    program.getValue().setProgramElement(FAT, progress);
                    break;
                case R.id.sb_carbo_gram:
                    program.getValue().setProgramElement(CARBO, progress);
                    break;
            }
        }
    }

    int getPercentSum() {
        return program.getValue() == null
                ? 0
                : program.getValue().getProtPct() + program.getValue().getFatPct() + program.getValue().getCarboPct();
    }

    void onFabClick() {
        if (program.getValue() == null) return;
        program.getValue().savePreference();
    }
}