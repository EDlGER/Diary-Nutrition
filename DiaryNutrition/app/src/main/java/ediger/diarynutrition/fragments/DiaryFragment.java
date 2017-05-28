package ediger.diarynutrition.fragments;


import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ediger.diarynutrition.Consts;
import ediger.diarynutrition.activity.AddActivity;
import ediger.diarynutrition.activity.FoodActivity;
import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.activity.WaterActivity;
import ediger.diarynutrition.adapters.RecordAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.dialogs.AddWaterDialog;
import ediger.diarynutrition.fragments.dialogs.AddWeightDialog;
import ediger.diarynutrition.objects.AppContext;
import info.abdolahi.CircularMusicProgressBar;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

public class DiaryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    View rootview;
    private long date;

    private List<List<Long>> buffer = new ArrayList<>();

    private SharedPreferences pref;
    private Intent intent;

    private Calendar nowto;
    private Calendar today = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    private RecordAdapter recordAdapter;
    private ExpandableListView listRecord;
    private View footerView;
    private TextView consCal;
    private TextView consCarbo;
    private TextView consProt;
    private TextView consFat;
    private TextView water;
    private TextView waterTotal;
    private TextView waterRemain;
    private CircularMusicProgressBar pbCal;
    private CircularMusicProgressBar pbCarbo;
    private CircularMusicProgressBar pbProt;
    private CircularMusicProgressBar pbFat;
    private CircularMusicProgressBar pbWater;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor;
        Calendar now = Calendar.getInstance();
        final MainActivity mainActivity = (MainActivity)getActivity();

        rootview = inflater.inflate(R.layout.fragment_diary, container, false);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        consCal = (TextView) rootview.findViewById(R.id.consCal);
        consCarbo = (TextView) rootview.findViewById(R.id.consCarbo);
        consProt = (TextView) rootview.findViewById(R.id.consProt);
        consFat = (TextView) rootview.findViewById(R.id.consFat);

        pbCal = (CircularMusicProgressBar) rootview.findViewById(R.id.pb_cal);
        pbCarbo = (CircularMusicProgressBar) rootview.findViewById(R.id.pb_carbo);
        pbProt = (CircularMusicProgressBar) rootview.findViewById(R.id.pb_prot);
        pbFat = (CircularMusicProgressBar) rootview.findViewById(R.id.pb_fat);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND,0);
        today.set(Calendar.MILLISECOND,0);

        nowto = Calendar.getInstance();
        nowto.clear(Calendar.MILLISECOND);
        nowto.clear(Calendar.SECOND);

        nowto.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), 0, 0);

        intent = getActivity().getIntent();
        date = intent.getLongExtra("date", nowto.getTimeInMillis());

        intent.removeExtra("date");
        now.setTimeInMillis(date);

        mainActivity.setCurrentDate(now.getTime());
        if (today.equals(now)) {
            mainActivity.setSubtitle(getString(R.string.diary_date_today));
        }

        AppContext.getDbDiary().setDate(date);

        cursor = AppContext.getDbDiary().getMealData();
        int[] groupTo = {
                R.id.txt_meal,
                R.id.txtGroupCal,
                R.id.txtGroupCarbo,
                R.id.txtGroupProt,
                R.id.txtGroupFat,
                R.id.txtGroupServ
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

        //R.layout.footer============================
        footerView = inflater.inflate(R.layout.record_footer, listRecord, false);

        CardView cardWater = (CardView) footerView.findViewById(R.id.card_water);
        cardWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View expansionView = rootview.findViewById(R.id.expansion_view);
                int location[] = new int[2];
                expansionView.getLocationInWindow(location);

                Intent intent = new Intent(getActivity(), WaterActivity.class);
                intent.putExtra(Consts.ARG_EXPANSION_LEFT_OFFSET, location[0]);
                intent.putExtra(Consts.ARG_EXPANSION_TOP_OFFSET, location[1]);
                intent.putExtra(Consts.ARG_EXPANSION_VIEW_WIDTH, expansionView.getWidth());
                intent.putExtra(Consts.ARG_EXPANSION_VIEW_HEIGHT, expansionView.getHeight());
                intent.putExtra("date", date);

                startActivity(intent);
            }
        });

        water = (TextView) footerView.findViewById(R.id.txt_water);
        waterTotal = (TextView) footerView.findViewById(R.id.txt_water_total);
        waterRemain = (TextView) footerView.findViewById(R.id.txt_water_remain);
        pbWater = (CircularMusicProgressBar) footerView.findViewById(R.id.pb_water);

        listRecord = (ExpandableListView) rootview.findViewById(R.id.listRecords);
        listRecord.addFooterView(footerView, null, false);
        listRecord.setAdapter(recordAdapter);
        registerForContextMenu(listRecord);

        for(int i=0; i < recordAdapter.getGroupCount(); i++) {
            listRecord.collapseGroup(i);
        }

        Loader loader = getLoaderManager().initLoader(-1, null, this);
        if (loader != null && !loader.isReset()){
            getLoaderManager().restartLoader(-1,null,this);
        } else {
            getLoaderManager().initLoader(-1, null, this);
        }

        setHeaderData();
        updateWaterUI();

        return rootview;
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);

        if (visible && isResumed())
        {
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!getUserVisibleHint()) {
            return;
        }

        final MainActivity mainActivity = (MainActivity)getActivity();

        mainActivity.menuMultipleActions.setVisibility(View.VISIBLE);
        mainActivity.menuMultipleActions.collapseImmediately();
        mainActivity.actionFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getActivity(), FoodActivity.class);
                addIntent.putExtra("CurrentCal", date);
                startActivity(addIntent);
            }
        });
        mainActivity.actionWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment a = new AddWeightDialog();
                a.show(getFragmentManager(), "add_weight_dialog");
                mainActivity.menuMultipleActions.collapse();
            }
        });
        mainActivity.actionWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWaterDialog();
                mainActivity.menuMultipleActions.collapse();
            }
        });

        mainActivity.datePicker.setVisibility(View.VISIBLE);

        mainActivity.mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {

            @Override
            public void onDayClick(Date dateClicked) {

                nowto.setTime(dateClicked);
                date = nowto.getTimeInMillis();
                AppContext.getDbDiary().setDate(date);

                if (today.equals(nowto)) {
                    mainActivity.setSubtitle(getString(R.string.diary_date_today));
                } else {
                    mainActivity.setSubtitle(dateFormat.format(dateClicked));
                }

                for (int i = 0; i < recordAdapter.getGroupCount(); i++) {
                    listRecord.expandGroup(i);
                    listRecord.collapseGroup(i);
                }
                setHeaderData();
                updateWaterUI();

                mainActivity.hideCalendarView();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mainActivity.setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });

        mainActivity.title.setPadding(0, 0, 0, 0);

        for(int i=0; i < recordAdapter.getGroupCount(); i++) {
            listRecord.expandGroup(i);
            listRecord.collapseGroup(i);
        }
        setHeaderData();
        updateWaterUI();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.
                ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childCount = recordAdapter.getChildrenCount(groupPos);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP &&
                listRecord.isGroupExpanded(groupPos) && childCount != 0) {
            menu.add(0, 1, 0, R.string.context_menu_copy);
        }

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            menu.add(0, 2, 0, R.string.context_menu_paste);
        }

        // Show context menu for childs
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            menu.add(0, 3, 0, R.string.context_menu_del);
            menu.add(0, 4, 0, R.string.context_menu_change);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.
                ExpandableListContextMenuInfo) item.getMenuInfo();
        int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (!listRecord.isGroupExpanded(groupPos)) {
            listRecord.expandGroup(groupPos);
        }
        Cursor child = this.recordAdapter.getChild(groupPos, childPos);
        long id;

        List<Long> childBuf;

        if (item.getItemId() == 1) {

            for (int i = 0; i < recordAdapter.getChildrenCount(groupPos); i++) {
                childBuf = new ArrayList<>();

                child = this.recordAdapter.getChild(groupPos, i);

                //food_id
                childBuf.add(child.getLong(4));
                //serving
                childBuf.add(child.getLong(1));
                //record_datetime
                childBuf.add(child.getLong(2));

                buffer.add(childBuf);
            }


            Snackbar.make(rootview, getString(R.string.message_record_meal_copy),
                    Snackbar.LENGTH_SHORT).show();

            return true;
        }

        if (item.getItemId() == 2) {

            for (int i = 0; i < buffer.size(); i++) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(buffer.get(i).get(2));
                calendar.clear(Calendar.MILLISECOND);
                calendar.clear(Calendar.SECOND);
                calendar.clear(Calendar.MINUTE);
                calendar.clear(Calendar.HOUR);
                calendar.clear(Calendar.HOUR_OF_DAY);

                AppContext.getDbDiary().addRec(
                        buffer.get(i).get(0),
                        (int) (long) buffer.get(i).get(1),
                        buffer.get(i).get(2) + (nowto.getTimeInMillis() - calendar.getTimeInMillis()),
                        groupPos + 1);

            }
            buffer.clear();

            listRecord.collapseGroup(groupPos);
            listRecord.expandGroup(groupPos);

            setHeaderData();

            return true;
        }

        if(item.getItemId() == 3) {
            id = child.getLong(0);
            AppContext.getDbDiary().delRec(id);

            listRecord.collapseGroup(groupPos);
            listRecord.expandGroup(groupPos);

            setHeaderData();

            return true;
        } else if (item.getItemId() == 4) {
            id = child.getLong(0);
            Intent intent = new Intent(getActivity(), AddActivity.class);
            intent.putExtra("recordId", id);
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }


    public void setHeaderData() {
        //Число без дробной части
        int res;
        int rem;
        int targetCal = pref.getInt("calories", 1);
        int targetCarbo = pref.getInt("carbo", 1);
        int targetProt = pref.getInt("prot", 1);
        int targetFat = pref.getInt("fat", 1);

        Cursor cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        long date = cursor.getLong(0);
        cursor.close();

        consCal.setText("0");
        consCarbo.setText("0");
        consProt.setText("0");
        consFat.setText("0");

        cursor = AppContext.getDbDiary().getDayData(date);
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
            rem = targetCal - res;
            consCal.setText(Integer.toString(rem));
            pbCal.setValue(res * 100 / targetCal);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO));
            rem = targetCarbo - res;
            consCarbo.setText(Integer.toString(rem));
            pbCarbo.setValue(res * 100 / targetCarbo);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT));
            rem = targetProt - res;
            consProt.setText(Integer.toString(rem));
            pbProt.setValue(res * 100 / targetProt);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT));
            rem = targetFat - res;
            consFat.setText(Integer.toString(rem));
            pbFat.setValue(res * 100 / targetFat);

        }
        cursor.close();
    }

    public void updateWaterUI() {
        int value = 0;
        //2000 заменить
        int target = pref.getInt(SettingsFragment.KEY_PREF_WATER, 2000);
        waterTotal.setText(String.valueOf(target));

        Cursor cursor = AppContext.getDbDiary().getDayWaterData(date);
        if (cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SUM_AMOUNT));
            water.setText(String.valueOf(value));
        }
        cursor.close();

        waterRemain.setText(String.valueOf(target - value));
        pbWater.setValue(value * 100 / target);
    }

    private void showWaterDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        AddWaterDialog dialog = new AddWaterDialog();
        dialog.setTargetFragment(this, 0);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
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

        /*int id = loader.getId();

        if (id != -1){
            try {
                recordAdapter.setChildrenCursor(id,null);
            } catch (NullPointerException e) {
                Log.w("TAG",e.getMessage());
            }
        } else {
            recordAdapter.setGroupCursor(null);
        }*/
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
            return db.getRecordData(cal, groupId);
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
            return db.getMealData();
        }
    }

}

