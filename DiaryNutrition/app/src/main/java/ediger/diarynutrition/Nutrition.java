package ediger.diarynutrition;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import ediger.diarynutrition.objects.AppContext;

/**
 * Created by ediger on 28.05.17.
 */

public final class Nutrition {

    public static final String KEY_PREF_GENDER = "gender";
    public static final String KEY_PREF_BIRTHDAY = "birthday";
    public static final String KEY_PREF_HEIGHT = "height";
    public static final String KEY_PREF_ACTIVITY = "activity";
    public static final String KEY_PREF_PURPOSE = "purpose";
    public static final String KEY_PREF_WEIGHT = "weight";

    public static final String KEY_PREF_WATER = "water";
    public static final String KEY_PREF_CALORIES = "calories";
    public static final String KEY_PREF_PROT = "prot";
    public static final String KEY_PREF_FAT = "fat";
    public static final String KEY_PREF_CARBO = "carbo";
    public static final String KEY_PREF_PROT_PERS = "prot_pers";
    public static final String KEY_PREF_FAT_PERS = "fat_pers";
    public static final String KEY_PREF_CARBO_PERS = "carbo_pers";

    private int [] loss = {30, 30, 40};
    private int [] deduction = {20, 30, 50};
    private int [] gain = {30, 20, 50};
    private int [][] purpose = {loss, deduction, gain};

    private SharedPreferences pref = PreferenceManager
            .getDefaultSharedPreferences(AppContext.getInstance().getBaseContext());

    public void savePreference(int prot, int fat, int carbo, int cal) {
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

    public void calculateCal() {
        int cal;
        float weight;
        float height;
        float age;
        float [] gender = {5,-161};
        double [] activity = {1.2, 1.375, 1.55, 1.725};
        int [] purpose = {-400, 0, 400};

        Calendar birthday = Calendar.getInstance();
        birthday.setTimeInMillis(pref.getLong(KEY_PREF_BIRTHDAY,0));
        age = Calendar.getInstance().get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

        weight = pref.getFloat(KEY_PREF_WEIGHT, 0);
        height = Float.parseFloat(pref.getString(KEY_PREF_HEIGHT,"0"));

        cal = (int) (10 * weight + 6.25 * height - 5 * age +
                gender[Integer.parseInt(pref.getString(KEY_PREF_GENDER, "1")) - 1]);
        cal *= activity[Integer.parseInt(pref.getString(KEY_PREF_ACTIVITY, "1")) - 1];
        cal += purpose[Integer.parseInt(pref.getString(KEY_PREF_PURPOSE, "1")) - 1];
    }

}
