package ediger.diarynutrition.activity;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.BillingFragment;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.fragments.SettingsFragment;
import ediger.diarynutrition.fragments.SummaryMainFragment;
import ediger.diarynutrition.fragments.WeightFragment;
import ediger.diarynutrition.util.IabHelper;
import ediger.diarynutrition.util.IabResult;
import ediger.diarynutrition.util.Inventory;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public FloatingActionsMenu menuMultipleActions;

    public Toolbar toolbar;

    public Boolean isFirstRun;

    public View actionA;

    public View actionB;

    public RelativeLayout datePicker;

    public TextView title;

    public CompactCalendarView mCompactCalendarView;

    public AppBarLayout mAppBarLayout;

    public SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    IabHelper mHelper;

    private static final String TAG = "DiaryNutrition";
    private static final String SKU_REMOVE_ADS = "com.ediger.removeads";
    private static final String PREF_FILE_PREMIUM = "premium_data";
    private static final String PREF_ADS_REMOVED = "ads_removed";

    private static final String PREF_FIRST_RUN = "first_run";

    private boolean isAdsRemoved;
    private boolean isExpanded = false;
    private long lastBackPress;
    private float mCurrentRotation = 360.0f;
    private ImageView arrow;
    private Toast backPressToast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //First app start
        isFirstRun = pref.getBoolean(PREF_FIRST_RUN, true);

        //startIntroActivity
        if (isFirstRun) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_FIRST_RUN, false);
        editor.apply();

        //Check ads
        isAdsRemoved = getSharedPreferences(PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_REMOVED, false);

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhuPm/qMEeS+CaCZfsFnMc8J5b6fkSlFihv" +
                        "ZwE7HlAC/hOHUinZLX3aG3m5sU3zMMTEVoCgR50/wgJt6BMFXri5V3+z+xgK4hkSHaX+L4" +
                        "djcoHgECyLjb73WwR1YbEOksQNaVM/MLIfBhtWfJuVaMwe0teAVRzGpxAPmTMB2jNYTxxt" +
                        "R9SMUwWt3VoU9lU1BJH8zd8TtPfqtSxTJbeNxe2i9/l2Ew4P2/J/BdeK1ZNnjeBM2Kz+S8" +
                        "5TuIuKhYle7x8DRf2CogSiIgZXf2Jnl+kU+vwr+Qw0QOYPwdLcakErx0ienUga3qmFTsCT" +
                        "sTS88zAFRXdFYDPip+CBrkpzbPhwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                    return;
                }

                if(mHelper == null) {
                    return;
                }

                Log.d(TAG, "In-app Billing setup is OK");
                mHelper.queryInventoryAsync(mReceivedInventoryListener);
            }
        });

        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_app_id));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        actionA = findViewById(R.id.action_a);
        actionB = findViewById(R.id.action_b);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                hideKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (isExpanded) {
                    hideCalendarView();
                }
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        datePicker = (RelativeLayout) findViewById(R.id.date_picker_button);
        title = (TextView) findViewById(R.id.title);

        mCompactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        // Force English
        mCompactCalendarView.setLocale(Locale.getDefault());

        mCompactCalendarView.setShouldDrawDaysHeader(true);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setSubtitle(dateFormat.format(dateClicked));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });

        // Set current date to today
        setCurrentDate(new Date());
        setSubtitle(getString(R.string.diary_date_today));

        arrow = (ImageView) findViewById(R.id.date_picker_arrow);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    RotateAnimation anim = new RotateAnimation(mCurrentRotation,
                            mCurrentRotation + 180.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);

                    mCurrentRotation = (mCurrentRotation + 180.0f) % 360.0f;
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    anim.setDuration(300);
                    arrow.startAnimation(anim);
                    mAppBarLayout.setExpanded(false, true);
                    isExpanded = false;
                } else {
                    RotateAnimation anim = new RotateAnimation(mCurrentRotation,
                            mCurrentRotation - 180.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);

                    mCurrentRotation = (mCurrentRotation - 180.0f) % 360.0f;
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    anim.setDuration(300);
                    arrow.startAnimation(anim);
                    mAppBarLayout.setExpanded(true, true);
                    isExpanded = true;
                }
            }
        });

        displayView(R.id.nav_diary);

    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            Log.d(TAG, "Query inventory finished");

            if (mHelper == null)
                return;

            if (result.isFailure()) {
                return;
            }
            Log.d(TAG, "Query inventory was successful");

            isAdsRemoved = inv.hasPurchase(SKU_REMOVE_ADS);
            SharedPreferences.Editor spe = getSharedPreferences(PREF_FILE_PREMIUM,
                    Context.MODE_PRIVATE).edit();
            spe.putBoolean(PREF_ADS_REMOVED, isAdsRemoved);
            spe.apply();
        }
    };

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    public void hideCalendarView() {

        RotateAnimation anim = new RotateAnimation(mCurrentRotation,
                mCurrentRotation + 180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        mCurrentRotation = (mCurrentRotation + 180.0f) % 360.0f;
        anim.setInterpolator(new LinearInterpolator());
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(150);
        arrow.startAnimation(anim);
        mAppBarLayout.setExpanded(false, true);
        isExpanded = false;
    }

    public void setCurrentDate(Date date) {
        setSubtitle(dateFormat.format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        TextView tvTitle = (TextView) findViewById(R.id.title);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setSubtitle(String subtitle) {
        TextView datePickerTextView = (TextView) findViewById(R.id.date_picker_text_view);

        if (datePickerTextView != null) {
            datePickerTextView.setText(subtitle);
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentTime - lastBackPress > 3000) {
                backPressToast = Toast.makeText(getBaseContext(), R.string.message_backpress,
                        Toast.LENGTH_LONG);
                backPressToast.show();
                lastBackPress = currentTime;
            } else {
                if (backPressToast != null) backPressToast.cancel();
                super.onBackPressed();
            }
        }
    }

    public void displayView(int viewId) {

        Fragment fragment = null;

        switch (viewId) {
            case R.id.nav_diary:
                fragment = new DiaryFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(0);
                }
                setTitle(getString(R.string.title_sec1));
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(4);
                }
                setTitle(getString(R.string.title_sec2));
                break;
            case R.id.nav_weight:
                fragment = new WeightFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(4);
                }
                setTitle(getString(R.string.title_sec3));
                break;
            case R.id.nav_stat:
                fragment = new SummaryMainFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(0);
                }
                setTitle(getString(R.string.title_sec4));
                break;
            case R.id.nav_rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" +
                                    this.getPackageName())));
                }
                break;
            case R.id.nav_donate:
                fragment = new BillingFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(0);
                }
                setTitle(getString(R.string.title_sec6));
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.contentMain, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
}


