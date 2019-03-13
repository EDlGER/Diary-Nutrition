package ediger.diarynutrition.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import ediger.diarynutrition.fragments.SettingsFragment;

public class DatabaseCopier {

    private static final String TAG = DatabaseCopier.class.getSimpleName();
    private static final String ASSET_DB_PATH = "databases";

    private static DatabaseCopier sInstance;


    private String mDatabasePath;
    private String mDatabaseName = "diary.db";

    private Context mContext;

    private DatabaseCopier(Context context) {
        mContext = context;
        mDatabasePath = mContext.getApplicationInfo().dataDir + "/" + ASSET_DB_PATH;
    }

    public static DatabaseCopier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseCopier(context);
        }
        return sInstance;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public void execute() {
        checkLanguage();

        File database = new File(mDatabasePath + "/" + mDatabaseName);

        if (!database.exists()) {
            copyDatabase(database);
        }
    }

    private void checkLanguage() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String dbLanguage = pref.getString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "");

        if (dbLanguage.isEmpty()) {
            SharedPreferences.Editor editor = pref.edit();
            if (Locale.getDefault().getLanguage().equals("ru")
                    || Locale.getDefault().getLanguage().equals("uk")) {
                dbLanguage = "ru";
                editor.putString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "ru");
            } else {
                dbLanguage = "en";
                editor.putString(SettingsFragment.KEY_PREF_DATA_LANGUAGE, "en");
            }
            editor.apply();
        }

        if (dbLanguage.contains("ru") || dbLanguage.contains("uk")) {
            mDatabaseName = "diary.db";
        } else {
            mDatabaseName = "diary_en.db";
        }

    }

    private void copyDatabase(File database) {

        String source = ASSET_DB_PATH + "/" + mDatabaseName;
        String dest = database.getPath();

        InputStream in;

        try {
            in = mContext.getAssets().open(source);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

            int length;
            byte[] buffer = new byte[8192];

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to write to data directory: " + dest +
                    "\n or missing database file in assets: " + source);
            e.printStackTrace();
        }

    }
























}
