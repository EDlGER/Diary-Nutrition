package ediger.diarynutrition.fragments;


import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import ediger.diarynutrition.R;
import ediger.diarynutrition.adapters.RecordAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.dialogs.DateDialog;
import ediger.diarynutrition.objects.AppContext;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

/**
 * Created by Ediger on 03.05.2015.
 *
 */
public class DiaryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private final int REQ_CODE_DATE = 1;

    View rootview;
    private long date;
    private String formatDate;
    private Calendar nowto;
    private Calendar today = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private RecordAdapter recordAdapter;
    private ExpandableListView listRecord;
    private Button btnDate;

    //Удалить все btn. Год,месяц,день перенести в локальные
    private Button btnAdd;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    int year;
    int month;
    int day;

    TextView txt; ///////


    @Override
    public void onResume() {
        for(int i=0; i < recordAdapter.getGroupCount(); i++) {
            listRecord.collapseGroup(i);
            listRecord.expandGroup(i);
        }
        setTxt();//////////
        super.onResume();
    }
    //                  header для ELV, подсчет БЖУ за день, дизайн
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Loader loader = getLoaderManager().initLoader(-1, null, this);
        if (loader != null && !loader.isReset()){
            getLoaderManager().restartLoader(-1,null,this);
        } else {
            getLoaderManager().initLoader(-1,null,this);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor;
        Calendar now = Calendar.getInstance();

        rootview = inflater.inflate(R.layout.diary_layout, container, false);

        txt = (TextView) rootview.findViewById(R.id.textView2);//////////
        setTxt();                                             /////////

        btnAdd = (Button) rootview.findViewById(R.id.b_add);
        btnDate = (Button) rootview.findViewById(R.id.datePicker);
        btnNext = (ImageButton) rootview.findViewById(R.id.btnDateNext);
        btnPrev = (ImageButton) rootview.findViewById(R.id.btnDatePrev);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnDate.setOnClickListener(this);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND,0);
        today.set(Calendar.MILLISECOND,0);

        nowto = Calendar.getInstance();
        nowto.clear(Calendar.MILLISECOND);
        nowto.clear(Calendar.SECOND);

        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH);
        day = now.get(Calendar.DAY_OF_MONTH);

        nowto.set(year, month, day, 0, 0);

        date = nowto.getTimeInMillis();
        now.setTimeInMillis(date);
        AppContext.getDbDiary().editDate(date);
        btnDate.setText(getString(R.string.diary_date_today));

        cursor = AppContext.getDbDiary().getMealData();
        int[] groupTo = {
                R.id.txt_meal,
                R.id.txtGroupCal,
                R.id.txtGroupCarbo,
                R.id.txtGroupProt,
                R.id.txtGroupFat
        };
        String[] groupFrom = AppContext.getDbDiary().getListMeal();
        int[] childTo = {
                R.id.txt_food_name,
                R.id.txt_cal,
                R.id.txt_carbo,
                R.id.txt_prot,
                R.id.txt_fat,
                R.id.txt_time,
                R.id.txt_serving

        };
        String[] childFrom = AppContext.getDbDiary().getListRecords();

        recordAdapter = new RecordAdapter(getActivity(),this, cursor,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom, groupTo,
                android.R.layout.simple_list_item_1,
                childFrom, childTo);

        listRecord = (ExpandableListView) rootview.findViewById(R.id.listRecords);
        listRecord.setAdapter(recordAdapter);
        registerForContextMenu(listRecord);
        for(int i=0; i < recordAdapter.getGroupCount(); i++) {
            listRecord.expandGroup(i);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), AddActivity.class);
                addIntent.putExtra("CurrentCal", date);
                startActivity(addIntent);
            }
        });
        return rootview;
    }

    private void setBtnDate() {

        if (today.equals(nowto)) {

            btnDate.setText(getString(R.string.diary_date_today));

        } else {
            formatDate = dateFormatter.format(nowto.getTime());
            btnDate.setText(formatDate);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnDateNext:
                nowto.set(year,month,day+1);
                day++;
                date = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(date);

                setBtnDate();

                for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                    listRecord.collapseGroup(i);
                    listRecord.expandGroup(i);
                }
                setTxt();                   ///////
                break;
            case R.id.btnDatePrev:
                nowto.set(year,month,day-1);
                day--;
                date = nowto.getTimeInMillis();
                AppContext.getDbDiary().editDate(date);

                setBtnDate();

                for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                    listRecord.collapseGroup(i);
                    listRecord.expandGroup(i);
                }
                setTxt();                   //////
                break;
            case R.id.datePicker:
                DialogFragment dialog = new DateDialog();
                dialog.setTargetFragment(DiaryFragment.this, REQ_CODE_DATE);
                dialog.show(getFragmentManager(), "date_dialog");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            int year = data.getIntExtra("year",0);
            int month = data.getIntExtra("month",0);
            int day = data.getIntExtra("day",0);

            this.day = day;
            nowto.set(year,month,day);
            date = nowto.getTimeInMillis();
            AppContext.getDbDiary().editDate(date);

            setBtnDate();

            for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                listRecord.collapseGroup(i);
                listRecord.expandGroup(i);
            }
            setTxt();                   //////
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.
                ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        // Show context menu for childs
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            menu.add(0, 3, 0, R.string.context_menu_del);
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
            long id = child.getLong(0);
            AppContext.getDbDiary().delRec(id);

            for(int i=0; i < recordAdapter.getGroupCount(); i++) {
                listRecord.collapseGroup(i);
                listRecord.expandGroup(i);
            }
            setTxt();                       /////////

            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void setTxt() {                  /////////
        Cursor cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        long date = cursor.getLong(0);
        cursor.close();
        txt.setText("0");
        cursor = AppContext.getDbDiary().getDayData(date);
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            txt.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CAL)));
        }
        cursor.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;

        if (id > -1){
            cl = new ChildCursorLoader(getActivity(),AppContext.getDbDiary(), date,id);
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
            if (!data.isClosed()) {
                HashMap<Integer,Integer> groupMap = recordAdapter.getGroupMap();
                try {
                    int groupPos = groupMap.get(id);
                    recordAdapter.setChildrenCursor(groupPos,data);
                } catch (NullPointerException e) {
                    Log.w("DEBUG",e.getMessage());
                }
            }
        } else {
            recordAdapter.setGroupCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();

        if (id != -1){
            try {
                recordAdapter.setChildrenCursor(id,null);
            } catch (NullPointerException e) {
                Log.w("TAG",e.getMessage());
            }
        } else {
            recordAdapter.setGroupCursor(null);
        }
    }

    private static class ChildCursorLoader extends CursorLoader {
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
            Cursor cursor = db.getRecordData(cal, groupId);
            return cursor;
        }
    }

    private static class GroupCursorLoader extends CursorLoader {
        DbDiary db;

        public GroupCursorLoader(Context context,DbDiary db) {
            super(context);
            this.db = db;
        }

        @Override
        public  Cursor loadInBackground() {
            Cursor cursor = db.getMealData();
            return cursor;
        }
    }

}

