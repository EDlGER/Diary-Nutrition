package ediger.diarynutrition.fragments;


import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.RecordAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * Created by Ediger on 03.05.2015.
 *
 */
public class diary_fragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    View rootview;
    private ExpandableListView listRecord;
    private Cursor cursor;
    private RecordAdapter recordAdapter;
    private Button btnAdd;
    private Button btnDate;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    String formatDate;
    public Calendar changeDate;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    int year;
    int month;
    int day;
    int[] arr;
    long cal;
    Calendar nowto;

    @Override
    public void onResume() {
        super.onResume();
       /* cursor = AppContext.getDbDiary().getRecords(cal);
        recordAdapter = new RecordAdapter(getActivity(),
                R.layout.record_item1,cursor, from,to,0);
        listRecord.setAdapter(recordAdapter);*/
    }

//        Сделать обновление данных после переключения даты (Loader!!!)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.diary_layout, container, false);

        Calendar now = Calendar.getInstance();
        nowto = Calendar.getInstance();


        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH);
        day = now.get(Calendar.DAY_OF_MONTH);

        nowto.set(year, month, day, 0, 0);

        cal = nowto.getTimeInMillis();
        now.setTimeInMillis(cal);
        AppContext.getDbDiary().editDate(cal);



        btnAdd = (Button) rootview.findViewById(R.id.b_add);
        btnDate = (Button) rootview.findViewById(R.id.datePicker);
        btnNext = (ImageButton) rootview.findViewById(R.id.btnDateNext);
        btnPrev = (ImageButton) rootview.findViewById(R.id.btnDatePrev);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);

        formatDate = dateFormatter.format(now.getTime());
        btnDate.setText(formatDate);


        cursor = AppContext.getDbDiary().getMealData();
        int[] groupTo = { android.R.id.text1 };
        String[] groupFrom = AppContext.getDbDiary().getListMeal();
        String[] childFrom = AppContext.getDbDiary().getListRecords();
        int[] childTo = {
                R.id.txt_food_name,
                R.id.txt_cal,
                R.id.txt_carbo,
                R.id.txt_prot,
                R.id.txt_fat,
                R.id.txt_time,
                R.id.txt_serving

        };

        recordAdapter = new RecordAdapter(getActivity(), cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, android.R.layout.simple_list_item_1, childFrom,
                childTo);



        listRecord = (ExpandableListView) rootview.findViewById(R.id.listRecords);
        listRecord.setAdapter(recordAdapter);
        registerForContextMenu(listRecord);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), AddActivity.class);
                addIntent.putExtra("CurrentCal",cal);
                startActivity(addIntent);
                getLoaderManager().getLoader(0).reset();
            }
        });

        getLoaderManager().initLoader(0, null, this);
        return rootview;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnDateNext:
                nowto.set(year,month,day+1);
                day++;
                cal = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(cal);
                formatDate = dateFormatter.format(nowto.getTime());
                btnDate.setText(formatDate);
                break;
            case R.id.btnDatePrev:
                nowto.set(year,month,day-1);
                day--;
                cal = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(cal);
                formatDate = dateFormatter.format(nowto.getTime());
                btnDate.setText(formatDate);
                break;
        }
    }

  /*  @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            arr = data.getIntArrayExtra("dateFromDialog");
            year = arr[0];
            month = arr[1];
            day = arr[2];
            changeDate.set(year,month,day);
            formatDate = dateFormatter.format(changeDate.getTime());
            btnDate.setText(formatDate);
        }
    }*/


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,3,0,R.string.context_menu_del);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 3){
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            AppContext.getDbDiary().delRec(acmi.id);

            /*cursor = AppContext.getDbDiary().getRecords(cal);
            recordAdapter = new RecordAdapter(getActivity(),
                    R.layout.record_item1,cursor, from,to,0);
            listRecord.setAdapter(recordAdapter);*/

            //getLoaderManager().getLoader(0).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(),AppContext.getDbDiary(),cal);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //recordAdapter.se
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class MyCursorLoader extends CursorLoader{
        DbDiary db;
        long cal;

        public MyCursorLoader(Context context, DbDiary db,long cal) {
            super(context);
            this.db = db;
            this.cal = cal;
        }

        @Override
        public  Cursor loadInBackground(){
            Cursor cursor = db.getRecords(cal);
            return cursor;
        }
    }
}

