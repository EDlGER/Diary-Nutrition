package ediger.diarynutrition;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 11.03.16.
 */
public class AddActivity extends AppCompatActivity {

    private int mealId = 1;
    private int gram = 1;
    private int hour;
    private int min;
    private long foodId;
    private long date;

    private  EditText txtTime;
    private EditText txtServ;
    private RadioGroup radioMeal;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        //Tittle
        final Intent intent = getIntent();
        foodId = intent.getLongExtra("FoodId",0);
        Cursor cursor = AppContext.getDbDiary().getNameFood(foodId);
        cursor.moveToFirst();
        String tittle = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        getSupportActionBar().setTitle(tittle);

        //Date
        cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        date = cursor.getLong(0);
        cursor.close();

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);

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
                } else if (position == 1) {
                    gram = 100;
                    txtServ.setText("1");
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
                AppContext.getDbDiary().addRec(foodId,
                        Integer.parseInt(txtServ.getText().toString()) * gram,
                        date, mealId);
                Intent intent1 = new Intent(AddActivity.this,MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            }
        });
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

    TimePickerDialog.OnTimeSetListener t= new TimePickerDialog.OnTimeSetListener() {
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
