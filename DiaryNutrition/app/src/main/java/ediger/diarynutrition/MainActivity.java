package ediger.diarynutrition;

import ediger.diarynutrition.diary.DiaryFragment;
import ediger.diarynutrition.inapputil.IabHelper;
import ediger.diarynutrition.inapputil.IabResult;
import ediger.diarynutrition.inapputil.Inventory;
import ediger.diarynutrition.intro.IntroActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;

import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import static ediger.diarynutrition.PreferenceHelper.*;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "DiaryNutrition";

    public AppBarLayout appBar;

    public Toolbar toolbar;

    public DrawerLayout drawerLayout;

    public NavController navController;

    public NavigationView navigationView;

    private IabHelper mHelper;

    private boolean isAdsRemoved;

    private long mLastBackPress;

    private Toast mBackPressToast;



    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Suppressed
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //First app start
        boolean isFirstRun = PreferenceHelper.getValue(Consts.KEY_FIRST_RUN, Boolean.class, true);

        //startIntroActivity
        if (isFirstRun) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
        }
        PreferenceHelper.setValue(Consts.KEY_FIRST_RUN, false);

        //Ads
        isAdsRemoved = getSharedPreferences(Consts.PREF_FILE_PREMIUM, Context.MODE_PRIVATE)
                .getBoolean(Consts.PREF_ADS_REMOVED, false);
        MobileAds.initialize(getApplicationContext());


        //TODO replace old billing
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

        setupNavigation();

    }

    private void setupNavigation() {
        appBar = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                hideKeyboard();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        NavigationUI.setupWithNavController(navigationView, navController);

        MenuItem rateItem = navigationView.getMenu().findItem(R.id.nav_rate);
        rateItem.setOnMenuItemClickListener(item -> {
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
            return false;
        });
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

            isAdsRemoved = inv.hasPurchase(Consts.SKU_REMOVE_ADS);
            SharedPreferences.Editor spe = getSharedPreferences(Consts.PREF_FILE_PREMIUM,
                    Context.MODE_PRIVATE).edit();
            spe.putBoolean(Consts.PREF_ADS_REMOVED, isAdsRemoved);
            spe.apply();
        }
    };

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(
                Navigation.findNavController(this, R.id.nav_host_fragment), drawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (navigationView.getMenu().findItem(R.id.nav_diary).isChecked()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastBackPress > 3000) {
                mBackPressToast = Toast.makeText(this, R.string.message_backpress,
                        Toast.LENGTH_LONG);
                mBackPressToast.show();
                mLastBackPress = currentTime;
            } else {
                if (mBackPressToast != null) mBackPressToast.cancel();
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        Fragment activeFragment = getSupportFragmentManager()
                .getPrimaryNavigationFragment()
                .getChildFragmentManager()
                .getPrimaryNavigationFragment();
        if (activeFragment instanceof DiaryFragment) {
            ((DiaryFragment) activeFragment).onContextMenuClosed();
        }
    }

    //TODO replace old billing
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
}


