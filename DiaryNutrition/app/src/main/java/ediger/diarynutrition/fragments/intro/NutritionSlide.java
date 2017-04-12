package ediger.diarynutrition.fragments.intro;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import ediger.diarynutrition.R;
import com.github.paolorotolo.appintro.ISlideSelectionListener;

public class NutritionSlide extends Fragment implements ISlideSelectionListener {

    private int cal;
    private int prot;
    private int fat;
    private int carbo;

    private int [] loss = {30, 30, 40};
    private int [] deduction = {20, 30, 50};
    private int [] gain = {30, 20, 50};
    private int [][] purpose = {loss, deduction, gain};

    private TextView txtCal;
    private TextView txtProt;
    private TextView txtFat;
    private TextView txtCarbo;

    private TextView txtProtPers;
    private TextView txtFatPers;
    private TextView txtCarboPers;

    private Calendar calendar = Calendar.getInstance();
    private SharedPreferences pref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_nutrition, container, false);

        txtCal = (TextView) view.findViewById(R.id.intro_cal);
        txtProt = (TextView) view.findViewById(R.id.txt_prot);
        txtFat = (TextView) view.findViewById(R.id.txt_fat);
        txtCarbo = (TextView) view.findViewById(R.id.txt_carbo);

        txtProtPers = (TextView) view.findViewById(R.id.txt_prot_percent);
        txtFatPers = (TextView) view.findViewById(R.id.txt_fat_percent);
        txtCarboPers = (TextView) view.findViewById(R.id.txt_carbo_percent);

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
        editor.putInt("calories", cal);
        editor.putInt("prot", prot);
        editor.putInt("fat", fat);
        editor.putInt("carbo", carbo);
        editor.putInt("prot_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][0]);
        editor.putInt("fat_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][1]);
        editor.putInt("carbo_pers", purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][2]);
        editor.apply();
    }

    private void calculateCal() {
        float weight;
        float height;
        float age;
        float [] gender = {5,-161};
        double [] activity = {1.2, 1.375, 1.55, 1.725};
        int [] purpose = {-400, 0, 400};
        Calendar birthday = Calendar.getInstance();

        weight = pref.getFloat("weight", 0);

        height = Float.parseFloat(pref.getString("height","0"));

        birthday.setTimeInMillis(pref.getLong("birthday",0));
        age = calendar.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

        cal = (int) (10 * weight + 6.25 * height - 5 * age +
                gender[Integer.parseInt(pref.getString("gender", "1")) - 1]);
        cal *= activity[Integer.parseInt(pref.getString("activity", "1")) - 1];
        cal += purpose[Integer.parseInt(pref.getString("purpose", "1")) - 1];
    }

    private void calculatePfc() {

        int purposeId = Integer.parseInt(pref.getString("purpose", "1")) - 1;

        prot = (int) ((double) purpose[purposeId][0] / 100 * (double) cal / 4);
        fat = (int) ((double) purpose[purposeId][1] / 100 * (double) cal / 9);
        carbo = (int) ((double) purpose[purposeId][2] / 100 * (double) cal / 4);

    }

    @Override
    public void onSlideSelected() {
        calculateCal();
        calculatePfc();
        txtCal.setText(String.valueOf(cal));

        txtProtPers.setText(purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][0] + "%");
        txtFatPers.setText(purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][1] + "%");
        txtCarboPers.setText(purpose[Integer.parseInt(pref.getString("purpose", "0")) - 1][2] + "%");

        txtProt.setText(String.valueOf(prot) + getResources().getString(R.string.add_rec_gram1));
        txtFat.setText(String.valueOf(fat) + getResources().getString(R.string.add_rec_gram1));
        txtCarbo.setText(String.valueOf(carbo) + getResources().getString(R.string.add_rec_gram1));

    }

    @Override
    public void onSlideDeselected() {

    }

}
