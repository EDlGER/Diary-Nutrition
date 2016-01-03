package ediger.diarynutrition.fragments;


import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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
        for(int i=0; i < recordAdapter.getGroupCount(); i++) {
            listRecord.collapseGroup(i);
            listRecord.expandGroup(i);
        }
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Loader loader = getLoaderManager().initLoader(-1, null, this);
        if (loader != null && !loader.isReset()){
            getLoaderManager().restartLoader(-1,null,this);
        }
        else {
            getLoaderManager().initLoader(-1,null,this);
        }
    }

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

        recordAdapter = new RecordAdapter(getActivity(),this, cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, android.R.layout.simple_list_item_1, childFrom,
                childTo);



        listRecord = (ExpandableListView) rootview.findViewById(R.id.listRecords);
        listRecord.setAdapter(recordAdapter);
        registerForContextMenu(listRecord);
        for(int i=0; i < recordAdapter.getGroupCount(); i++)
            listRecord.expandGroup(i);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), AddActivity.class);
                addIntent.putExtra("CurrentCal", cal);
                startActivity(addIntent);
            }
        });

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
                for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                    listRecord.collapseGroup(i);
                    listRecord.expandGroup(i);
                }
                break;
            case R.id.btnDatePrev:
                nowto.set(year,month,day-1);
                day--;
                cal = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(cal);
                formatDate = dateFormatter.format(nowto.getTime());
                btnDate.setText(formatDate);
                for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                    listRecord.collapseGroup(i);
                    listRecord.expandGroup(i);
                }
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
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.
                ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);


        // Show context menu for childs
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            menu.add(0,3,0,R.string.context_menu_del);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 3) {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.
                    ExpandableListContextMenuInfo) item.getMenuInfo();
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
            Cursor child = this.recordAdapter.getChild(groupPos, childPos);
            child.moveToFirst();
            long id = child.getLong(0);
            AppContext.getDbDiary().delRec(id);

            for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                listRecord.collapseGroup(i);
                listRecord.expandGroup(i);
            }

            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //return new MyCursorLoader(getActivity(),AppContext.getDbDiary(),cal);
        CursorLoader cl;
        if (id > -1){
            cl = new ChildCursorLoader(getActivity(),AppContext.getDbDiary(),cal,id);
        }
        else {
            cl = new GroupCursorLoader(getActivity(),AppContext.getDbDiary());
        }
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id > -1){
            if (!data.isClosed()){
                HashMap<Integer,Integer> groupMap = recordAdapter.getGroupMap();
                try {
                    int groupPos = groupMap.get(id);
                    recordAdapter.setChildrenCursor(groupPos,data);
                } catch (NullPointerException e) {
                    Log.w("DEBUG",e.getMessage());
                }
            }
        }
        else {
            recordAdapter.setGroupCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*int id = loader.getId();
        if (id != -1){
            try {
                recordAdapter.setChildrenCursor(id,null);
            } catch (NullPointerException e) {
                Log.w("TAG",e.getMessage());
            }
        }
        else {
            recordAdapter.setGroupCursor(null);
        }*/
    }

    /*private static class MyCursorLoader extends CursorLoader{
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
    }*/
    private static class ChildCursorLoader extends CursorLoader{
        DbDiary db;
        long cal;
        int groupId;

        public ChildCursorLoader(Context context,DbDiary db,long cal,int groupId) {
            super(context);
            this.db = db;
            this.cal = cal;
            this.groupId = groupId;
        }
        @Override
        public  Cursor loadInBackground(){
            Cursor cursor = db.getRecordData(cal,groupId);
            return cursor;
        }
    }

    private static class GroupCursorLoader extends CursorLoader{
        DbDiary db;

        public GroupCursorLoader(Context context,DbDiary db) {
            super(context);
            this.db = db;
        }
        @Override
        public  Cursor loadInBackground(){
            Cursor cursor = db.getMealData();
            return cursor;
        }
    }
}

