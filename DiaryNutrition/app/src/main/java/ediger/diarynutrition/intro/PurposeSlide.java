package ediger.diarynutrition.intro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ediger.diarynutrition.AppContext;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.databinding.FragmentIntroPurposeBinding;
import ediger.diarynutrition.util.SnackbarUtils;
import ediger.diarynutrition.weight.WeightViewModel;

import static ediger.diarynutrition.PreferenceHelper.KEY_PURPOSE;
import static ediger.diarynutrition.PreferenceHelper.KEY_WEIGHT;

import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.ISlideSelectionListener;

import java.util.Calendar;

public class PurposeSlide extends Fragment implements ISlidePolicy, ISlideSelectionListener {

    private FragmentIntroPurposeBinding mBinding;

    private WeightViewModel mViewModel;

    private int purposeId = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_purpose, container, false);

        mViewModel = new WeightViewModel(AppContext.getInstance());

        mBinding.rgPurpose.setOnCheckedChangeListener((group, checkedId) -> {
            AppCompatRadioButton checkedButton = mBinding.rgPurpose.findViewById(checkedId);
            purposeId = mBinding.rgPurpose.indexOfChild(checkedButton) + 1;
        });

        return mBinding.getRoot();
    }

    @Override
    public boolean isPolicyRespected() {
        String weight = mBinding.edWeight.getText().toString();
        return !weight.equals("") && !weight.equals("0");
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        SnackbarUtils.showSnackbar(getView(), getString(R.string.intro_weight_error));
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        PreferenceHelper.setValue(KEY_PURPOSE, String.valueOf(purposeId));
        PreferenceHelper.setValue(KEY_WEIGHT, Float.parseFloat(mBinding.edWeight.getText().toString()));
        mViewModel.addWeight(new Weight(Float.parseFloat(mBinding.edWeight.getText().toString()),
                Calendar.getInstance().getTimeInMillis()));
    }
}
