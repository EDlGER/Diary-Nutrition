package ediger.diarynutrition;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferenceHelper {
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
