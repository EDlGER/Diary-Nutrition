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

import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.FragmentIntroNutritionBinding;
import ediger.diarynutrition.util.NutritionProgramUtils;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_nutrition, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onSlideSelected() {

        NutritionProgramUtils.setToDefault();
        initValues();

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

    private void initValues() {
        cal = PreferenceHelper.getValue(KEY_PROGRAM_CAL, Float.class, 0f);
        prot = PreferenceHelper.getValue(KEY_PROGRAM_PROT, Float.class, 0f);
        fat = PreferenceHelper.getValue(KEY_PROGRAM_FAT, Float.class, 0f);
        carbo = PreferenceHelper.getValue(KEY_PROGRAM_CARBO, Float.class, 0f);
        protPercent = PreferenceHelper.getValue(KEY_PROGRAM_PROT_PERCENT, Integer.class, 0);
        fatPercent = PreferenceHelper.getValue(KEY_PROGRAM_FAT_PERCENT, Integer.class, 0);
        carboPercent = PreferenceHelper.getValue(KEY_PROGRAM_CARBO_PERCENT, Integer.class, 0);
    }

}
