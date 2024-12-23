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

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.R;
import ediger.diarynutrition.databinding.FragmentIntroActivityBinding;

import com.github.paolorotolo.appintro.ISlideSelectionListener;

public class ActivitySlide extends Fragment implements ISlideSelectionListener {

    private FragmentIntroActivityBinding mBinding;

    private int activityId = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_activity, container, false);

        final String [] info = {
                getString(R.string.intro_activity_1),
                getString(R.string.intro_activity_2),
                getString(R.string.intro_activity_3),
                getString(R.string.intro_activity_4)};

        mBinding.activityInfo.setText(info[0]);

        mBinding.rgActivity.setOnCheckedChangeListener((group, checkedId) -> {
            AppCompatRadioButton checkedButton = mBinding.rgActivity.findViewById(checkedId);
            activityId = mBinding.rgActivity.indexOfChild(checkedButton) + 1;

            mBinding.activityInfo.setText(info[activityId - 1]);
        });

        return mBinding.getRoot();
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        PreferenceHelper.setValue(Consts.KEY_ACTIVITY, String.valueOf(activityId));
    }

}
