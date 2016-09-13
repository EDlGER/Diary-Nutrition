package ediger.diarynutrition.activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

public class AddActivity extends AppCompatActivity {

    private int mealId = 1;
    private int gram = 1;
    private int hour;
    private int min;
    private long foodId;
    private long date;

    private TextView cal;
    private TextView carbo;
    private TextView prot;
    private TextView fat;

    private EditText txtTime;
    private EditText txtServ;
    private RadioGroup radioMeal;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cal = (TextView) findViewById(R.id.info_cal1);
        carbo = (TextView) findViewById(R.id.info_carbo1);
        prot = (TextView) findViewById(R.id.info_prot1);
        fat = (TextView) findViewById(R.id.info_fat1);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        AppCompatSpinner spinner = (AppCompatSpinner) findViewById(R.id.spServ);
        txtTime = (EditText) findViewById(R.id.txtTime);
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

        //Title
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.title_text);
        final Intent intent = getIntent();
        foodId = intent.getLongExtra("FoodId",0);
        Cursor cursor = AppContext.getDbDiary().getNameFood(foodId);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        getSupportActionBar().setTitle(title);


        //Date
        cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        date = cursor.getLong(0);
        cursor.close();

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);

        if (0 <= hour && hour < 10) radioMeal.check(R.id.rb_meal1);
        if (9 < hour && hour < 12) radioMeal.check(R.id.rb_meal2);
        if (11 < hour && hour < 15) radioMeal.check(R.id.rb_meal3);
        if (14 < hour && hour < 18) radioMeal.check(R.id.rb_meal4);
        if (17 < hour && hour < 24) radioMeal.check(R.id.rb_meal5);

        AppCompatRadioButton checkedButton = (AppCompatRadioButton)
                radioMeal.findViewById(radioMeal.getCheckedRadioButtonId());
        mealId = radioMeal.indexOfChild(checkedButton) + 1;

        calendar.setTimeInMillis(date);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hour, min);
        txtTime.setText(timeFormatter.format(calendar.getTime()));
        date = calendar.getTimeInMillis();


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
                    txtServ.setText("100");
                    txtServ.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
                    setInfo();
                } else if (position == 1) {
                    gram = 100;
                    txtServ.setText("1");
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
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                if (txtServ.getText().toString().matches("")) {
                    Snackbar.make(v, getString(R.string.message_serving), Snackbar.LENGTH_SHORT).show();
                } else {
                    AppContext.getDbDiary().addRec(foodId,
                            Integer.parseInt(txtServ.getText().toString()) * gram,
                            date, mealId);
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH), 0, 0);
                    calendar.clear(Calendar.SECOND);
                    calendar.clear(Calendar.MILLISECOND);
                    date = calendar.getTimeInMillis();

                    Intent intent1 = new Intent(AddActivity.this, MainActivity.class);
                    intent1.putExtra("date",date);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setInfo() {
        Cursor cursor = AppContext.getDbDiary().getFood(foodId);
        cursor.moveToFirst();

        if (!this.txtServ.getText().toString().matches("")) {
            float serv = Float.parseFloat(this.txtServ.getText().toString());
            float cal = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL)) / 100 * serv * gram;
            float carbo = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)) / 100 * serv * gram;
            float prot = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT)) / 100 * serv * gram;
            float fat = cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT)) / 100 * serv * gram;

            this.cal.setText(String.format(Locale.getDefault(), "%.1f", cal));
            this.carbo.setText(String.format(Locale.getDefault(), "%.1f",carbo));
            this.prot.setText(String.format(Locale.getDefault(), "%.1f",prot));
            this.fat.setText(String.format(Locale.getDefault(), "%.1f",fat));
        } else {
            this.cal.setText("0");
            this.carbo.setText("0");
            this.prot.setText("0");
            this.fat.setText("0");
        }
        cursor.close();
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
