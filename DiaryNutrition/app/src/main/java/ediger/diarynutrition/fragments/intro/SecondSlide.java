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
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Calendar;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 12.05.16.
 */
public class SecondSlide extends Fragment {

    private static final String KEY_PREF_PURPOSE = "purpose";

    private int purposeId = 1;

    private SharedPreferences pref;
    private RadioGroup radioPurpose;
    private EditText txtWeight;
    private Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_2, container, false);

        txtWeight = (EditText) view.findViewById(R.id.ed_weight);

        radioPurpose = (RadioGroup) view.findViewById(R.id.rg_purpose);
        radioPurpose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioPurpose.findViewById(checkedId);
                purposeId = radioPurpose.indexOfChild(checkedButton) + 1;
            }
        });

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!txtWeight.getText().toString().equals("") ||
                !txtWeight.getText().toString().equals("0")) {
            AppContext.getDbDiary().addWeight(calendar.getTimeInMillis(),
                    Float.parseFloat(txtWeight.getText().toString()));
        } else {
            txtWeight.setText("70");
        }

        savePreference();
    }

    private void savePreference() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_PURPOSE,String.valueOf(purposeId));
        editor.apply();
    }

}
