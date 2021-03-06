package ediger.diarynutrition.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
import ediger.diarynutrition.adapters.WaterFooterAdapter;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.dialogs.AddWaterDialog;
import ediger.diarynutrition.fragments.dialogs.AddWeightDialog;
import ediger.diarynutrition.objects.AppContext;
import info.abdolahi.CircularMusicProgressBar;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

public class DiaryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener {

    View rootview;
    private long mDate;
    private boolean isRemaining = true;

    private List<List<Long>> buffer = new ArrayList<>();

    private SharedPreferences pref;

    private Calendar nowto;
    private Calendar today = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    private RecyclerView.Adapter wrappedAdapter;
    private RecordAdapter recordAdapter;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;

    //Values in CircularProgress
    private TextView consCal;
    private TextView consCarbo;
    private TextView consProt;
    private TextView consFat;

    private CircularMusicProgressBar pbCal;
    private CircularMusicProgressBar pbCarbo;
    private CircularMusicProgressBar pbProt;
    private CircularMusicProgressBar pbFat;
    private TextSwitcher headerSwitcher;

    AddWaterDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.v("DiaryFragment", "onCreateViewIn");

        Cursor cursor;
        Calendar now = Calendar.getInstance();
        final MainActivity mainActivity = (MainActivity)getActivity();

        rootview = inflater.inflate(R.layout.fragment_diary, container, false);

        setHasOptionsMenu(true);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        consCal = rootview.findViewById(R.id.consCal);
        consCarbo = rootview.findViewById(R.id.consCarbo);
        consProt = rootview.findViewById(R.id.consProt);
        consFat = rootview.findViewById(R.id.consFat);

        pbCal = rootview.findViewById(R.id.pb_cal);
        pbCarbo = rootview.findViewById(R.id.pb_carbo);
        pbProt = rootview.findViewById(R.id.pb_prot);
        pbFat =  rootview.findViewById(R.id.pb_fat);

        //Header switcher
        headerSwitcher = rootview.findViewById(R.id.headerSwitcher);
        headerSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(getActivity());
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setTextSize(15);
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                return textView;
            }
        });
        Animation inAnimation = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_out);
        headerSwitcher.setInAnimation(inAnimation);
        headerSwitcher.setOutAnimation(outAnimation);
        headerSwitcher.setText(isRemaining ?
                getResources().getString(R.string.diary_header_remain) :
                getResources().getString(R.string.diary_header_total));

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND,0);
        today.set(Calendar.MILLISECOND,0);

        nowto = Calendar.getInstance();
        nowto.clear(Calendar.MILLISECOND);
        nowto.clear(Calendar.SECOND);

        nowto.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), 0, 0);

        Intent intent = getActivity().getIntent();
        mDate = intent.getLongExtra("date", nowto.getTimeInMillis());

        intent.removeExtra("date");
        now.setTimeInMillis(mDate);

        mainActivity.setCurrentDate(now.getTime());
        if (today.equals(now)) {
            mainActivity.setSubtitle(getString(R.string.diary_date_today));
        }

        AppContext.setDate(mDate);

        cursor = AppContext.getDbDiary().getMealData();

        mRecyclerView = rootview.findViewById(R.id.list_records);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(savedInstanceState);
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this);
        mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(this);

        recordAdapter = new RecordAdapter(getActivity(), this, cursor, this);

        wrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(recordAdapter);

        if (pref.getBoolean(SettingsFragment.KEY_PREF_UI_WATER_CARD, true)) {

            View.OnClickListener onWaterClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor cursor = AppContext.getDbDiary().getWaterData(mDate);
                    if (cursor.moveToFirst()) {
                        View expansionView = rootview.findViewById(R.id.expansion_view);
                        int location[] = new int[2];
                        expansionView.getLocationInWindow(location);

                        Intent intent = new Intent(getActivity(), WaterActivity.class);
                        intent.putExtra(Consts.ARG_EXPANSION_LEFT_OFFSET, location[0]);
                        intent.putExtra(Consts.ARG_EXPANSION_TOP_OFFSET, location[1]);
                        intent.putExtra(Consts.ARG_EXPANSION_VIEW_WIDTH, expansionView.getWidth());
                        intent.putExtra(Consts.ARG_EXPANSION_VIEW_HEIGHT, expansionView.getHeight());
                        intent.putExtra("date", mDate);

                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.message_card_water),
                                Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                }
            };

            wrappedAdapter = new WaterFooterAdapter(getActivity().getBaseContext(),
                    wrappedAdapter, onWaterClickListener);
        }

        mRecyclerView.setAdapter(wrappedAdapter);  // requires *wrapped* adapter

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);

        //Animation
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Need to disable them when using animation indicator.
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
        registerForContextMenu(mRecyclerView);

        setHeaderData();
        getLoaderManager().initLoader(-1, null, this);

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
                startActivity(addIntent);
            }
        });
        mainActivity.actionWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment a = new AddWeightDialog();
                a.show(getFragmentManager(), "addWeightDialog");
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
                mDate = nowto.getTimeInMillis();
                AppContext.setDate(mDate);

                if (today.equals(nowto)) {
                    mainActivity.setSubtitle(getString(R.string.diary_date_today));
                } else {
                    mainActivity.setSubtitle(dateFormat.format(dateClicked));
                }
                setHeaderData();
                restartChildrenLoaders();

                mainActivity.hideCalendarView();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mainActivity.setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });

        mainActivity.title.setPadding(0, 0, 0, 0);

        setHeaderData();
        wrappedAdapter.notifyDataSetChanged();
    }

    private void restartChildrenLoaders() {
        for (int i = 0; i < recordAdapter.getGroupMap().size() + 1; i++) {
            getLoaderManager().restartLoader(i, null, this);
        }
        mRecyclerViewExpandableItemManager.collapseAll();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        RecyclerView rv = RecyclerViewAdapterUtils.getParentRecyclerView(v);
        if (rv == null) {
            return;
        }
        RecyclerView.ViewHolder vh = rv.findContainingViewHolder(v);

        int rootPosition = vh.getAdapterPosition();
        if (rootPosition == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.Adapter rootAdapter = rv.getAdapter();
        int localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter, recordAdapter, rootPosition);

        long expandablePosition = mRecyclerViewExpandableItemManager.getExpandablePosition(localFlatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
        int childCount = recordAdapter.getChildrenCount(groupPosition);

        if (childPosition == RecyclerView.NO_POSITION) {
            //group
            if (mRecyclerViewExpandableItemManager.isGroupExpanded(groupPosition) && childCount != 0) {
                menu.add(0, 1, 0, R.string.context_menu_copy).setActionView(v);
            }
            menu.add(0, 2, 0, R.string.context_menu_paste).setActionView(v);
        } else {
            //child
            menu.add(0, 3, 0, R.string.context_menu_del).setActionView(v);
            menu.add(0, 4, 0, R.string.context_menu_change).setActionView(v);
        }

        recordAdapter.setContextMenuState(true);
    }

    public void onContextMenuClosed() {
        recordAdapter.setContextMenuState(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        RecyclerView rv = RecyclerViewAdapterUtils.getParentRecyclerView(item.getActionView());
        if (rv == null) {
            return super.onContextItemSelected(item);
        }
        RecyclerView.ViewHolder vh = rv.findContainingViewHolder(item.getActionView());

        int rootPosition = vh.getAdapterPosition();
        if (rootPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        RecyclerView.Adapter rootAdapter = rv.getAdapter();
        int localFlatPosition = WrapperAdapterUtils.unwrapPosition(rootAdapter, recordAdapter, rootPosition);

        long expandablePosition = mRecyclerViewExpandableItemManager.getExpandablePosition(localFlatPosition);
        int groupPosition = RecyclerViewExpandableItemManager.getPackedPositionGroup(expandablePosition);
        int childPosition = RecyclerViewExpandableItemManager.getPackedPositionChild(expandablePosition);
        int childCount = recordAdapter.getChildrenCount(groupPosition);

        Cursor child = recordAdapter.getChild(groupPosition, childPosition);
        long id;
        List<Long> childBuf;

        switch (item.getItemId()) {
            //Meal copy
            case 1:
                for (int i = 0; i < childCount; i++) {
                    childBuf = new ArrayList<>();

                    child = this.recordAdapter.getChild(groupPosition, i);

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
                break;
            //Meal insert
            case 2:
                if (buffer.size() == 0) {
                    Snackbar.make(rootview, getString(R.string.message_record_meal_insert_fail),
                            Snackbar.LENGTH_SHORT).show();
                    break;
                }
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
                            buffer.get(i).get(1).intValue(),
                            buffer.get(i).get(2) + (nowto.getTimeInMillis() - calendar.getTimeInMillis()),
                            groupPosition + 1);

                }
                restartChildrenLoaders();
                mRecyclerViewExpandableItemManager.notifyChildrenOfGroupItemChanged(groupPosition);
                buffer.clear();
                setHeaderData();

                Snackbar.make(rootview, getString(R.string.message_record_meal_insert),
                        Snackbar.LENGTH_SHORT).show();

                break;

            //Record delete
            case 3:
                id = child.getLong(0);
                AppContext.getDbDiary().delRec(id);
                mRecyclerViewExpandableItemManager.notifyChildItemRemoved(groupPosition, childPosition);
                for (int i = 0; i < recordAdapter.getGroupMap().size() + 1; i++) {
                    getLoaderManager().restartLoader(i, null, this);
                }
                if (childPosition == 0 && recordAdapter.getChildrenCount(groupPosition) == 1) {
                    mRecyclerViewExpandableItemManager.collapseGroup(groupPosition);
                }
                setHeaderData();
                break;

            //Record changing
            case 4:
                id = child.getLong(0);
                Intent intent = new Intent(getActivity(), AddActivity.class);
                intent.putExtra("recordId", id);
                startActivity(intent);
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_diary, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch:
                isRemaining = !isRemaining;
                headerSwitcher.setText(isRemaining ?
                        getResources().getString(R.string.diary_header_remain) :
                        getResources().getString(R.string.diary_header_total));
                setHeaderData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setHeaderData() {
        //Число без дробной части
        int res;
        int rem;
        int targetCal = pref.getInt("calories", 1);
        int targetCarbo = pref.getInt("carbo", 1);
        int targetProt = pref.getInt("prot", 1);
        int targetFat = pref.getInt("fat", 1);

        if (targetCal == 0) targetCal++;
        if (targetCarbo == 0) targetCarbo++;
        if (targetProt == 0) targetProt++;
        if (targetFat == 0) targetFat++;

        long date = AppContext.getDate();

        consCal.setText("0");
        consCarbo.setText("0");
        consProt.setText("0");
        consFat.setText("0");

        Cursor cursor = AppContext.getDbDiary().getDayData(date);
        if (cursor.moveToFirst()) {
            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
            rem = targetCal - res;
            consCal.setText(isRemaining ? String.valueOf(rem) : String.valueOf(res));
            pbCal.setValue(res * 100 / targetCal);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CARBO));
            rem = targetCarbo - res;
            consCarbo.setText(isRemaining ? String.valueOf(rem) : String.valueOf(res));
            pbCarbo.setValue(res * 100 / targetCarbo);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_PROT));
            rem = targetProt - res;
            consProt.setText(isRemaining ? String.valueOf(rem) : String.valueOf(res));
            pbProt.setValue(res * 100 / targetProt);

            res = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_FAT));
            rem = targetFat - res;
            consFat.setText(isRemaining ? String.valueOf(rem) : String.valueOf(res));
            pbFat.setValue(res * 100 / targetFat);
        }
        cursor.close();
    }

    private void showWaterDialog() {
        dialog = new AddWaterDialog();
        dialog.setOnWaterUpdateListener(new AddWaterDialog.OnWaterUpdateListener() {
            @Override
            public void onUpdate() {
                wrappedAdapter.notifyDataSetChanged();
            }
        });
            getFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(android.R.id.content, dialog)
                    .addToBackStack(null)
                    .commit();
    }

    @Override
    public void onGroupCollapse(int groupPosition, boolean fromUser, Object payload) {

    }

    @Override
    public void onGroupExpand(int groupPosition, boolean fromUser, Object payload) {
        if (fromUser) {
            adjustScrollPositionOnGroupExpanded(groupPosition);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.list_item_height);
        int topMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 16); // top-spacing: 16dp
        int bottomMargin = topMargin; // bottom-spacing: 16dp

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;

        if (id > -1){
            cl = new ChildCursorLoader(getActivity(),AppContext.getDbDiary(), mDate, id);
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
                try {
                    HashMap<Integer,Integer> groupMap = recordAdapter.getGroupMap();
                    int groupPos = groupMap.get(id);
                    recordAdapter.setChildrenCursor(groupPos, data);
                } catch (NullPointerException e) {
                    //Log.w("DEBUG", e.getMessage());
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
                //Log.w("TAG",e.getMessage());
            }
        } else {
            recordAdapter.setGroupCursor(null);
        }
    }

    private static class ChildCursorLoader extends CursorLoader {
        DbDiary db;
        long date;
        int groupId;

        public ChildCursorLoader(Context context,DbDiary db,long date,int groupId) {
            super(context);
            this.db = db;
            this.date = date;
            this.groupId = groupId;
        }

        @Override
        public  Cursor loadInBackground(){
            return db.getRecordData(date, groupId);
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

