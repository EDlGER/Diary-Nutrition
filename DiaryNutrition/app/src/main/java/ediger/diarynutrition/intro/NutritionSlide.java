package ediger.diarynutrition.intro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import ediger.diarynutrition.NutritionProgramManager;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.FragmentIntroNutritionBinding;
import static ediger.diarynutrition.PreferenceHelper.*;

import com.github.paolorotolo.appintro.ISlideSelectionListener;

public class NutritionSlide extends Fragment implements ISlideSelectionListener {

    private FragmentIntroNutritionBinding mBinding;

    private float cal;
    private float prot;
    private float fat;
    private float carbo;
    private int protPercent;
    private int fatPercent;
    private int carboPercent;
    private int water;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_nutrition, container, false);

        return mBinding.getRoot();
    }

    private void calculateProgram() {
        NutritionProgramManager nutrition = NutritionProgramManager.getInstance();
        nutrition.updateProgram();
        cal = nutrition.getCal();
        prot = nutrition.getProt();
        fat = nutrition.getFat();
        carbo = nutrition.getCarbo();
        protPercent = nutrition.getProtPercent();
        fatPercent = nutrition.getFatPercent();
        carboPercent = nutrition.getCarboPercent();
        water = nutrition.getWater();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreference();
    }

    private void savePreference() {
        PreferenceHelper.setValue(KEY_PROGRAM_CAL, cal);
        PreferenceHelper.setValue(KEY_PROGRAM_PROT, prot);
        PreferenceHelper.setValue(KEY_PROGRAM_FAT, fat);
        PreferenceHelper.setValue(KEY_PROGRAM_CARBO, carbo);
        PreferenceHelper.setValue(KEY_PROGRAM_PROT_PERCENT, protPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_FAT_PERCENT, fatPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_CARBO_PERCENT, carboPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_WATER, water);
    }

    @Override
    public void onSlideSelected() {
        calculateProgram();
        mBinding.txtCal.setText(String.valueOf((int) cal));

        mBinding.txtProtPercent.setText(String.format(Locale.getDefault(), "%d%%", protPercent));
        mBinding.txtFatPercent.setText(String.format(Locale.getDefault(), "%d%%", fatPercent));
        mBinding.txtCarboPercent.setText(String.format(Locale.getDefault(), "%d%%", carboPercent));

        mBinding.txtProt.setText(String.format(Locale.getDefault(), "%d%s", (int) prot, getString(R.string.add_rec_gram1)));
        mBinding.txtFat.setText(String.format(Locale.getDefault(), "%d%s", (int) fat, getString(R.string.add_rec_gram1)));
        mBinding.txtCarbo.setText(String.format(Locale.getDefault(), "%d%s", (int) carbo, getString(R.string.add_rec_gram1)));
    }

    @Override
    public void onSlideDeselected() {

    }

}
