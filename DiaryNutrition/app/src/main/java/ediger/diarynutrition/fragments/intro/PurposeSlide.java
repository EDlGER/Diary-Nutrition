package ediger.diarynutrition.fragments.intro;

import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.Toast;

import java.util.Calendar;

import ediger.diarynutrition.R;
import ediger.diarynutrition.objects.AppContext;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.ISlideSelectionListener;

public class PurposeSlide extends Fragment implements ISlidePolicy, ISlideSelectionListener {

    private static final String KEY_PREF_PURPOSE = "purpose";
    private static final String KEY_PREF_WEIGHT = "weight";

    private int purposeId = 1;

    private SharedPreferences pref;
    private RadioGroup radioPurpose;
    private EditText txtWeight;
    private Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_purpose, container, false);

        txtWeight = (EditText) view.findViewById(R.id.ed_weight);
        txtWeight.setText("70");

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

    private void savePreference() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PREF_PURPOSE,String.valueOf(purposeId));
        editor.putFloat(KEY_PREF_WEIGHT,Float.parseFloat(txtWeight.getText().toString()));
        editor.apply();
    }

    private void saveWeight() {
        long date;

        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        date = calendar.getTimeInMillis();

        Cursor cursor = AppContext.getDbDiary().getWeight(date);

        if (cursor.moveToFirst()) {
            AppContext.getDbDiary().setWeight(date, Float.parseFloat(txtWeight.getText().toString()));
        } else {
            AppContext.getDbDiary().addWeight(date, Float.parseFloat(txtWeight.getText().toString()));
        }
        cursor.close();
    }

    @Override
    public boolean isPolicyRespected() {
        if (txtWeight.getText().toString().equals("") || txtWeight.getText().toString().equals("0")) {
            return false;
        }
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        Toast.makeText(getContext(), R.string.intro_weight_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {
        saveWeight();
        savePreference();
    }
}
