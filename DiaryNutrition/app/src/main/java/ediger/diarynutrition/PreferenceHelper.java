package ediger.diarynutrition;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferenceHelper {

    public static final String KEY_GENDER = "gender";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_PURPOSE = "purpose";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_ACTIVITY = "activity";

    public static final String KEY_PROGRAM_CAL = "calories";
    public static final String KEY_PROGRAM_PROT = "prot";
    public static final String KEY_PROGRAM_FAT = "fat";
    public static final String KEY_PROGRAM_CARBO = "carbo";
    public static final String KEY_PROGRAM_PROT_PERCENT = "prot_pers";
    public static final String KEY_PROGRAM_FAT_PERCENT = "fat_percs";
    public static final String KEY_PROGRAM_CARBO_PERCENT = "carbo_pers";
    public static final String KEY_PROGRAM_WATER = "water";

    public static final String KEY_PREF_UI_WATER_CARD = "water_card";
    public static final String PREF_WATER_SERVING_1 = "water_serving_1";
    public static final String PREF_WATER_SERVING_2 = "water_serving_2";
    public static final String PREF_WATER_SERVING_3 = "water_serving_3";
    public static final String PREF_WATER_SERVING_4 = "water_serving_4";

    public static final String PREF_FILE_PREMIUM = "premium_data";
    public static final String PREF_ADS_REMOVED = "ads_removed";

    static final String KEY_FIRST_RUN = "first_run";
    static final String SKU_REMOVE_ADS = "com.ediger.removeads";

    private static SharedPreferences sPref;

    private PreferenceHelper() {

    }

    static void init(Context context) {
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPreferences() {
        return sPref;
    }

    public static <T> void setValue(String key, T value) {
        if (value instanceof String) {
            edit(editor -> editor.putString(key, (String) value));
        } else if (value instanceof Boolean) {
            edit(editor -> editor.putBoolean(key, (Boolean) value));
        } else if (value instanceof Integer) {
            edit(editor ->  editor.putInt(key, (Integer) value));
        } else  if (value instanceof Long) {
            edit(editor -> editor.putLong(key, (Long) value));
        } else if (value instanceof Float) {
            edit(editor ->  editor.putFloat(key, (Float) value));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static <T> T getValue(String key, Class<?> aClass, T defaultValue) {
        Object value;
        if (aClass.equals(String.class)) {
            value = sPref.getString(key, (String) defaultValue);
        } else if (aClass.equals(Boolean.class)) {
            value = sPref.getBoolean(key, (Boolean) defaultValue);
        } else if (aClass.equals(Integer.class)) {
            value = sPref.getInt(key, (Integer) defaultValue);
        } else if (aClass.equals(Float.class)) {
            value = sPref.getFloat(key, (Float) defaultValue);
        } else if (aClass.equals(Long.class)) {
            value = sPref.getLong(key, (Long) defaultValue);
        } else {
            throw new UnsupportedOperationException();
        }
        return (T) value;
    }

    private static void edit(Performer<SharedPreferences.Editor> performer) {
        SharedPreferences.Editor editor = sPref.edit();
        performer.performOperation(editor);
        editor.apply();
    }

    public interface Performer<T> {
        void performOperation(T victim);
    }

}
