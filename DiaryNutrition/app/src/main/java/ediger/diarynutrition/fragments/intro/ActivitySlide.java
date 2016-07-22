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

import ediger.diarynutrition.R;

/**
 * Created by root on 12.05.16.
 */
public class ActivitySlide extends Fragment {

    private static final String KEY_PREF_ACTIVITY = "activity";

    private int activityId = 1;

    private SharedPreferences pref;
    private RadioGroup radioActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_activity, container, false);


        radioActivity = (RadioGroup) view.findViewById(R.id.rg_activity);
        radioActivity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioActivity.findViewById(checkedId);
                activityId = radioActivity.indexOfChild(checkedButton) + 1;
            }
        });

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        savePreference();
    }

    private void savePreference() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_ACTIVITY,String.valueOf(activityId));
        editor.apply();
    }

}
