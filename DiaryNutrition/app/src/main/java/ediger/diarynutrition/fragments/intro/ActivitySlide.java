package ediger.diarynutrition.fragments.intro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import ediger.diarynutrition.R;
import com.github.paolorotolo.appintro.ISlideSelectionListener;

public class ActivitySlide extends Fragment implements ISlideSelectionListener{

    private static final String KEY_PREF_ACTIVITY = "activity";

    private int activityId = 1;

    private SharedPreferences pref;
    private RadioGroup radioActivity;
    private TextView txtActivityInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_activity, container, false);

        final String [] info = {
                getString(R.string.intro_activity_1),
                getString(R.string.intro_activity_2),
                getString(R.string.intro_activity_3),
                getString(R.string.intro_activity_4)};

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        txtActivityInfo = (TextView) view.findViewById(R.id.activity_info);
        txtActivityInfo.setText(info[0]);

        radioActivity = (RadioGroup) view.findViewById(R.id.rg_activity);
        radioActivity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioActivity.findViewById(checkedId);
                activityId = radioActivity.indexOfChild(checkedButton) + 1;

                txtActivityInfo.setText(info[activityId - 1]);
            }
        });

        return view;
    }


    private void savePreference() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_ACTIVITY,String.valueOf(activityId));
        editor.apply();
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        savePreference();
    }

}
