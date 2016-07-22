package ediger.diarynutrition.activity;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.fragments.SettingsFragment;
import ediger.diarynutrition.fragments.SummaryFragment;
import ediger.diarynutrition.fragments.WeightFragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public FloatingActionsMenu menuMultipleActions;

    public Boolean isFirstRun;

    public View actionA;

    public View actionB;

    public RelativeLayout datePicker;

    public TextView title;

    public CompactCalendarView mCompactCalendarView;

    public SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    private static String PREF_FIRST_RUN = "first_run";

    private boolean isExpanded = false;
    private float mCurrentRotation = 360.0f;
    private ImageView arrow;
    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //First app start
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isFirstRun = pref.getBoolean(PREF_FIRST_RUN, true);

        //startActivity
        if (isFirstRun) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_FIRST_RUN, false);
        editor.apply();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        mCompactCalendarView.drawSmallIndicatorForEvents(false);

        // Force English
        mCompactCalendarView.setLocale(/*Locale.getDefault()*/Locale.ENGLISH);

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

        //RelativeLayout datePickerButton = (RelativeLayout) findViewById(R.id.date_picker_button);

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void displayView(int viewId) {

        Fragment fragment = null;

        //String title = getString(R.string.app_name);

        switch (viewId) {
            case R.id.nav_diary:
                fragment = new DiaryFragment();
                setTitle(getString(R.string.title_sec1));
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                setTitle(getString(R.string.title_sec2));
                break;
            case R.id.nav_weight:
                fragment = new WeightFragment();
                setTitle(getString(R.string.title_sec3));
                break;
            case R.id.nav_stat:
                fragment = new SummaryFragment();
                setTitle(getString(R.string.title_sec4));
                break;

        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.contentMain, fragment);
            ft.commit();
        }

        // set the toolbar title
        //if (getSupportActionBar() != null) {
        //    getSupportActionBar().setTitle(title);
        //}

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

}


