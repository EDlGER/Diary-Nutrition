package ediger.diarynutrition;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 11.03.16.
 */
public class AddActivity extends AppCompatActivity {

    private int index;
    private int mealId = 1;
    private long foodId;
    private long date;

    private EditText txtServ;
    private RadioGroup radioMeal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radioMeal = (RadioGroup) findViewById(R.id.rgMeal);
        txtServ = (EditText) findViewById(R.id.txtServ);

        final Intent intent = getIntent();
        foodId = intent.getLongExtra("FoodId",0);

        Cursor cursor = AppContext.getDbDiary().getNameFood(foodId);
        cursor.moveToFirst();
        String tittle = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        getSupportActionBar().setTitle(tittle);

        //Дата
        cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        date = cursor.getLong(0);
        cursor.close();

        radioMeal.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AppCompatRadioButton checkedButton = (AppCompatRadioButton) radioMeal.findViewById(checkedId);
                mealId = radioMeal.indexOfChild(checkedButton) + 1;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppContext.getDbDiary().addRec(foodId, Integer.parseInt(txtServ.getText().toString()),
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
}
