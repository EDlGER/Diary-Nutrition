package ediger.diarynutrition.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.IntRange;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import ediger.diarynutrition.activity.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.objects.AppContext;
import ediger.diarynutrition.widget.ExpandableItemIndicator;

public class RecordAdapter extends CursorTreeRecyclerAdapter<RecordAdapter.MyGroupViewHolder, RecordAdapter.MyChildViewHolder> {

    private DiaryFragment mFragment;

    protected final HashMap<Integer, Integer> mGroupMap;

    private interface Expandable extends ExpandableItemConstants {
    }

    public static abstract class MyBaseViewHolder extends AbstractExpandableItemViewHolder {
        public FrameLayout mContainer;
        public TextView mTextView;

        public MyBaseViewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    public static class MyGroupViewHolder extends MyBaseViewHolder {
        public ExpandableItemIndicator mIndicator;

        public MyGroupViewHolder(View v) {
            super(v);
            mIndicator = (ExpandableItemIndicator) v.findViewById(R.id.indicator);
        }
    }

    public static class MyChildViewHolder extends MyBaseViewHolder {
        public MyChildViewHolder(View v) {
            super(v);
        }
    }

    public RecordAdapter( Context context, DiaryFragment fragment, Cursor cursor) {
        super(context, cursor);
        mFragment = fragment;
        mGroupMap = new HashMap<>();
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_group_item, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item, parent, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, Cursor cursor) {
        // set text
        holder.mTextView.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_M_NAME)));

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;
            boolean isExpanded;
            boolean animateIndicator = ((expandState & Expandable.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);

            if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                bgResId = R.drawable.bg_group_item_expanded_state;
                isExpanded = true;
            } else {
                bgResId = R.drawable.bg_group_item_normal_state;
                isExpanded = false;
            }

            holder.mContainer.setBackgroundResource(bgResId);
            holder.mIndicator.setExpandedState(isExpanded, animateIndicator);
        }
    }

    @Override
    public void onBindChildViewHolder(MyChildViewHolder holder, Cursor cursor) {
        // set text
        holder.mTextView.setText("*");

        // set background resource (target view ID: container)
        int bgResId;
        bgResId = R.drawable.bg_item_normal_state;
        holder.mContainer.setBackgroundResource(bgResId);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        Loader loader = null;
        int groupPos = groupCursor.getPosition();
        int groupId = groupCursor.getInt(groupCursor
                .getColumnIndex(DbDiary.ALIAS_ID));

        mGroupMap.put(groupId, groupPos);

//        if (mFragment != null && mFragment.isAdded()) {
//            loader = mFragment.getLoaderManager().getLoader(groupId);
//            if (loader != null && !loader.isReset()){
//                mFragment.getLoaderManager().restartLoader(groupId,null,mFragment);
//            }
//            else {
//                mFragment.getLoaderManager().initLoader(groupId,null,mFragment);
//            }
//        }
        return null;
    }

    public HashMap<Integer, Integer> getGroupMap () {
        return mGroupMap;
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        return false;
    }

/*
    public RecordAdapter(Context context,DiaryFragment df, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        mContext = context;
        mActivity = (MainActivity) context;
        mFragment = df;
        this.layoutInflater = layoutInflater.from(context);
        mGroupMap = new HashMap<Integer, Integer>();
    }

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm");

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private LayoutInflater layoutInflater;


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;

        //Калории без дробной части
        int cal;

        Cursor cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        long date = cursor.getLong(0);
        cursor.close();

        if (view == null ) {
            view = layoutInflater.inflate(R.layout.record_group_item1,parent,false);
        }

        RelativeLayout relative = (RelativeLayout) view.findViewById(R.id.group_layout);
        View divider = view.findViewById(R.id.child_divider);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relative.getLayoutParams();

        Resources r = mContext.getResources();
        int pxSide = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 8,
                r.getDisplayMetrics() );
        int pxBottomCol = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 4,
                r.getDisplayMetrics() );
        int pxBottomExp = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, -3,
                r.getDisplayMetrics() );


        if((!isExpanded) || (getChildrenCount(groupPosition) == 0)) {
            params.setMargins(pxSide,0,pxSide,pxBottomCol);
            relative.setLayoutParams(params);
            divider.setVisibility(View.INVISIBLE);
        }
        if ((isExpanded) && (getChildrenCount(groupPosition) != 0)) {
            params.setMargins(pxSide,0,pxSide,pxBottomExp);
            relative.setLayoutParams(params);
            divider.setVisibility(View.VISIBLE);
        }

        TextView group_name = (TextView) view.findViewById(R.id.txt_meal);
        TextView group_cal = (TextView) view.findViewById(R.id.txtGroupCal);
        TextView group_carbo = (TextView) view.findViewById(R.id.txtGroupCarbo);
        TextView group_prot = (TextView) view.findViewById(R.id.txtGroupProt);
        TextView group_fat = (TextView) view.findViewById(R.id.txtGroupFat);
        TextView group_serv = (TextView) view.findViewById(R.id.txtGroupServ);

        Cursor c = AppContext.getDbDiary().getMealData();
        c.moveToPosition(groupPosition);

        group_name.setText(c.getString(c.getColumnIndex(DbDiary.ALIAS_M_NAME)));

        c = AppContext.getDbDiary().getGroupData(date);
        int mealID;

        group_cal.setText("");
        group_carbo.setText("");
        group_prot.setText("");
        group_fat.setText("");
        group_serv.setText("");

        if (c.moveToFirst()) {
            do {
                mealID = c.getInt(0);
                if ( mealID == groupPosition + 1){
                    cal = (int) c.getFloat(c.getColumnIndex(DbDiary.ALIAS_CAL));

                    group_cal.setText(Integer.toString(cal));

                    String carbo = String.format(Locale.getDefault(), "%.1f",c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_CARBO)));
                    String prot = String.format(Locale.getDefault(), "%.1f", c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_PROT)));
                    String fat = String.format(Locale.getDefault(), "%.1f", c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_FAT)));
                    String serv = c.getString(c.getColumnIndex(DbDiary.ALIAS_SERVING)) + mContext.getString(R.string.elv_gram);

                    group_carbo.setText(carbo);
                    group_prot.setText(prot);
                    group_fat.setText(fat);
                    group_serv.setText(serv);
                }
            } while (c.moveToNext());
        }
        c.close();

        return view;
    }


    @Override
    public View newChildView(Context context, Cursor cursor,boolean isLastChild, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.record_item1, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.food_name = (TextView) view.findViewById(R.id.txt_food_name);
        holder.carbo = (TextView) view.findViewById(R.id.txt_carbo);
        holder.prot = (TextView) view.findViewById(R.id.txt_prot);
        holder.fat = (TextView) view.findViewById(R.id.txt_fat);
        holder.cal = (TextView) view.findViewById(R.id.txt_cal);
        holder.time = (TextView) view.findViewById(R.id.txt_time);
        holder.serving = (TextView) view.findViewById(R.id.txt_serving);

        view.setTag(holder);

        return view;
    }



    @Override
    public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ViewHolder holder = (ViewHolder) view.getTag();

        RelativeLayout relative = (RelativeLayout) view.findViewById(R.id.list_item_layout);
        View divider = view.findViewById(R.id.child_divider2);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relative.getLayoutParams();

        Resources r = mContext.getResources();
        int pxSide = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 8,
                r.getDisplayMetrics() );
        int pxTop = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, -2,
                r.getDisplayMetrics() );
        int pxBottom = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, -2,
                r.getDisplayMetrics() );
        int pxBottomLast = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                r.getDisplayMetrics());

        //Калории без дробной части
        int cal = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));

        String carbo = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        String prot = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        String fat = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_FAT)));

        String name = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        if (name.length() > 25) {
            holder.food_name.setTextSize(12);
        } else {
            holder.food_name.setTextSize(16);
        }

        if (isLastChild) {
            params.setMargins(pxSide,pxTop,pxSide,pxBottomLast);
            relative.setLayoutParams(params);
            divider.setVisibility(View.INVISIBLE);
        } else {
            params.setMargins(pxSide, pxTop, pxSide, pxBottom);
            relative.setLayoutParams(params);
            divider.setVisibility(View.VISIBLE);
        }

        holder.food_name.setText(name);
        holder.cal.setText(Integer.toString(cal));
        holder.carbo.setText(carbo);
        holder.prot.setText(prot);
        holder.fat.setText(fat);

        holder.serving.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_SERVING))
                + mContext.getString(R.string.elv_gram));

        long dateTime = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_RECORD_DATETIME));
        calendar.setTimeInMillis(dateTime);

        holder.time.setText(timeFormatter.format(calendar.getTime()));
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
     return true;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        Loader loader = null;
        int groupPos = groupCursor.getPosition();
        int groupId = groupCursor.getInt(groupCursor.
                getColumnIndex(DbDiary.ALIAS_ID));

        mGroupMap.put(groupId, groupPos);

        if (mFragment != null && mFragment.isAdded()) {
            loader = mFragment.getLoaderManager().getLoader(groupId);
            if (loader != null && !loader.isReset()){
                mFragment.getLoaderManager().restartLoader(groupId,null,mFragment);
            }
            else {
                mFragment.getLoaderManager().initLoader(groupId,null,mFragment);
            }
        }
        return null;
    }

    public HashMap<Integer,Integer> getGroupMap(){
        return mGroupMap;
    }

    private class ViewHolder {
        public TextView food_name;
        public TextView cal;
        public TextView carbo;
        public TextView prot;
        public TextView fat;
        public TextView time;
        public TextView serving;
    }*/
}
