package ediger.diarynutrition.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.AppContext;

public class AddActivity extends AppCompatActivity {

    private static final String PREF_ADS_REMOVED = "ads_removed";
    private static final String PREF_FILE_PREMIUM = "premium_data";

    private boolean isAdsRemoved;

    private int mealId = 1;
    private int gram = 1;
    private int serving = 100;

    //Нужны для TimePickerDialog
    private int hour;
    private int min;

    private long foodId;
    private long date;
    private long recordId;

    private Cursor cursor;

    private TextView cal;
    private TextView carbo;
    private TextView prot;
    private TextView fat;

    private TextView txtTime;
    private EditText txtServ;
    private AppCompatCheckBox cbAddMoreFood;
    private RadioGroup radioMeal;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        SharedPreferences pref = getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE);
        isAdsRemoved = pref.getBoolean(PREF_ADS_REMOVED, false);

        mInterstitialAd = new InterstitialAd(this);

        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_inter_id));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("E8B8A18280F8D8867639D5CF594195AD")
                .build();

        if (!isAdsRemoved) {
            mInterstitialAd.loadAd(adRequest);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cal = (TextView) findViewById(R.id.info_cal1);
        carbo = (TextView) findViewById(R.id.info_carbo1);
        prot = (TextView) findViewById(R.id.info_prot1);
        fat = (TextView) findViewById(R.id.info_fat1);
        cbAddMoreFood = (AppCompatCheckBox) findViewById(R.id.cb_add_food);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        AppCompatSpinner spinner = (AppCompatSpinner) findViewById(R.id.spServ);
        txtTime = (TextView) findViewById(R.id.txt_time);
        txtServ = (EditText) findViewById(R.id.txtServ);
        radioMeal = (RadioGroup) findViewById(R.id.rgMeal);

        //Spinner
        String[] spData = {
                getString(R.string.add_rec_gram1),
                getString(R.string.add_rec_gram100)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,spData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final Intent intent = getIntent();
        recordId = intent.getLongExtra("recordId", -1);

        //Если вызов от FoodActivity
        if (recordId == -1) {
            foodId = intent.getLongExtra("FoodId", 0);

            //date = AppContext.getDate();
            date = 1111111;

            hour = calendar.get(Calendar.HOUR_OF_DAY);
            min = calendar.get(Calendar.MINUTE);

            if (0 <= hour && hour < 10) radioMeal.check(R.id.rb_meal1);
            if (9 < hour && hour < 12) radioMeal.check(R.id.rb_meal2);
            if (11 < hour && hour < 15) radioMeal.check(R.id.rb_meal3);
            if (14 < hour && hour < 18) radioMeal.check(R.id.rb_meal4);
            if (17 < hour && hour < 24) radioMeal.check(R.id.rb_meal5);

            calendar.setTimeInMillis(date);
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour, min);
            txtTime.setText(timeFormatter.format(calendar.getTime()));
            date = calendar.getTimeInMillis();
            //Если вызов от DiaryFragment
        } else {
            int[] meal = {
                    R.id.rb_meal1,
                    R.id.rb_meal2,
                    R.id.rb_meal3,
                    R.id.rb_meal4,
                    R.id.rb_meal5
            };

            cursor = AppContext.Companion.getDbDiary().getRecordById(recordId);
            cursor.moveToFirst();
            foodId = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_ID));

            serving = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SERVING));
            date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_RECORD_DATETIME));
            calendar.setTimeInMillis(date);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            min = calendar.get(Calendar.MINUTE);

            txtServ.setText(String.valueOf(serving));
            txtTime.setText(timeFormatter.format(calendar.getTime()));

            mealId = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_MEAL_ID));
            radioMeal.check(meal[mealId - 1]);
            cursor.close();
        }

        //Title
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.title_text);
        cursor = AppContext.Companion.getDbDiary().getNameFood(foodId);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        cursor.close();
        getSupportActionBar().setTitle(title);

        AppCompatRadioButton checkedButton = (AppCompatRadioButton)
                radioMeal.findViewById(radioMeal.getCheckedRadioButtonId());
        mealId = radioMeal.indexOfChild(checkedButton) + 1;

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddActivity.this,t,hour,min,true).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    gram = 1;

                    if (recordId == -1)
                        txtServ.setText("");
                    else
                        txtServ.setText(String.valueOf(serving));

                    txtServ.setHint(R.string.dialog_serv_std);
                    txtServ.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
                    setInfo();
                } else if (position == 1) {
                    gram = 100;
                    txtServ.setText("");
                    txtServ.setHint(R.string.serv_nstd);
                    txtServ.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                    setInfo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        radioMeal.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioMeal.findViewById(checkedId);
                mealId = radioMeal.indexOfChild(checkedButton) + 1;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                int serving;

                if (txtServ.getText().toString().startsWith("0")) {
                    Snackbar.make(v, getString(R.string.message_serving), Snackbar.LENGTH_SHORT).show();
                } else {

                    if (txtServ.getText().toString().matches("")) {
                        serving = gram == 1 ? 100 : 1;
                    } else {
                        serving = Integer.parseInt(txtServ.getText().toString());
                    }

                    if (recordId == -1) {
                        AppContext.Companion.getDbDiary().addRec(foodId,
                                serving * gram,
                                date, mealId);
                    } else {
                        AppContext.Companion.getDbDiary().editRec(recordId, foodId,
                                serving * gram,
                                date, mealId);
                    }

                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH), 0, 0);
                    calendar.clear(Calendar.SECOND);
                    calendar.clear(Calendar.MILLISECOND);
                    date = calendar.getTimeInMillis();

                    if (cbAddMoreFood.isChecked()) {
                        onBackPressed();
                    } else {
                        Intent intent1 = new Intent(AddActivity.this, MainActivity.class);
                        intent1.putExtra("date", date);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                    }
                }
            }
        });

        txtServ.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isAdsRemoved) mInterstitialAd.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setInfo() {
        Cursor cursor = AppContext.Companion.getDbDiary().getFood(foodId);
        cursor.moveToFirst();
        float serv;

        if (txtServ.getText().toString().matches("")) {
            if (gram == 1) {
                serv = 100;
            } else {
                serv = 1;
            }
        } else {
            serv = Float.parseFloat(txtServ.getText().toString());
        }

        float cal = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL)) / 100 * serv * gram;
        float carbo = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)) / 100 * serv * gram;
        float prot = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT)) / 100 * serv * gram;
        float fat = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT)) / 100 * serv * gram;

        this.cal.setText(String.format(Locale.getDefault(), "%.1f", cal));
        this.carbo.setText(String.format(Locale.getDefault(), "%.1f",carbo));
        this.prot.setText(String.format(Locale.getDefault(), "%.1f",prot));
        this.fat.setText(String.format(Locale.getDefault(), "%.1f",fat));

        cursor.close();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour, min);
            txtTime.setText(timeFormatter.format(calendar.getTime()));
            date = calendar.getTimeInMillis();
        }
    };
}
