package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.objects.AppContext;
import ediger.diarynutrition.widget.ExpandableItemIndicator;

public class RecordAdapter extends CursorTreeRecyclerAdapter <RecordAdapter.MyGroupViewHolder, RecordAdapter.MyChildViewHolder> {

    private DiaryFragment mFragment;
    private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
    private boolean isContextMenuOpen = false;

    protected final HashMap<Integer, Integer> mGroupMap;

    private interface Expandable extends ExpandableItemConstants {
    }

    public static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        FrameLayout mBackground;
        RelativeLayout mContainer;
        TextView mMeal;
        TextView mServing;
        TextView mCal;
        TextView mProt;
        TextView mFat;
        TextView mCarbo;
        ExpandableItemIndicator mIndicator;
        View mDivider;


        public MyGroupViewHolder(View v) {
            super(v);
            mBackground = v.findViewById(R.id.background);
            mContainer = v.findViewById(R.id.container);
            mMeal = v.findViewById(R.id.txt_meal);
            mServing = v.findViewById(R.id.txt_group_serv);
            mCal = v.findViewById(R.id.txt_group_cal);
            mProt = v.findViewById(R.id.txt_group_prot);
            mFat = v.findViewById(R.id.txt_group_fat);
            mCarbo = v.findViewById(R.id.txt_group_carbo);
            mIndicator = v.findViewById(R.id.indicator);
            mDivider = v.findViewById(R.id.divider);
        }
    }

    public static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        FrameLayout mBackground;
        RelativeLayout mContainer;
        TextView mFoodName;
        TextView mCal;
        TextView mProt;
        TextView mFat;
        TextView mCarbo;
        TextView mTime;
        TextView mServing;
        View mDivider;

        public MyChildViewHolder(View v) {
            super(v);
            mBackground = v.findViewById(R.id.background);
            mContainer = v.findViewById(R.id.container);
            mFoodName = v.findViewById(R.id.txt_food_name);
            mCal = v.findViewById(R.id.txt_cal);
            mProt = v.findViewById(R.id.txt_prot);
            mFat = v.findViewById(R.id.txt_fat);
            mCarbo = v.findViewById(R.id.txt_carbo);
            mTime = v.findViewById(R.id.txt_time);
            mServing = v.findViewById(R.id.txt_serving);
            mDivider = v.findViewById(R.id.divider);
        }

    }

    public RecordAdapter(Context context, DiaryFragment fragment, Cursor cursor) {
        super(context, cursor, false);
        mFragment = fragment;
        mGroupMap = new HashMap<>();

        mOnCreateContextMenuListener = null;
    }

    public RecordAdapter(Context context, DiaryFragment fragment, Cursor cursor,
                         View.OnCreateContextMenuListener listener) {
        super(context, cursor, false);
        mFragment = fragment;
        mGroupMap = new HashMap<>();

        mOnCreateContextMenuListener = listener;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_record_group_item, parent, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_record_child_item, parent, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder holder, Cursor cursor) {

        holder.mContainer.setOnCreateContextMenuListener(mOnCreateContextMenuListener);

        int groupId = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_ID));
        int groupPos = cursor.getPosition();

        if (getChildrenCount(groupPos) != 0) {
            Cursor c = AppContext.getDbDiary().getGroupData(AppContext.getDate(), groupId);

            c.moveToFirst();

            //TODO Change receiving values in different formats
            String serv = c.getString(c.getColumnIndex(DbDiary.ALIAS_SERVING))
                    + mContext.getString(R.string.elv_gram);
            int cal = (int) c.getFloat(c.getColumnIndex(DbDiary.ALIAS_CAL));
            String prot = String.format(Locale.getDefault(), "%.1f",
                    c.getFloat(c.getColumnIndex(DbDiary.ALIAS_PROT)));
            String fat = String.format(Locale.getDefault(), "%.1f",
                    c.getFloat(c.getColumnIndex(DbDiary.ALIAS_FAT)));
            String carbo = String.format(Locale.getDefault(), "%.1f",
                    c.getFloat(c.getColumnIndex(DbDiary.ALIAS_CARBO)));
            holder.mServing.setText(serv);
            holder.mCal.setText(String.valueOf(cal));
            holder.mProt.setText(prot);
            holder.mFat.setText(fat);
            holder.mCarbo.setText(carbo);

            c.close();
        } else {
            holder.mServing.setText("");
            holder.mCal.setText("");
            holder.mProt.setText("");
            holder.mFat.setText("");
            holder.mCarbo.setText("");
        }

        holder.mMeal.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_M_NAME)));

        // mark as clickable
        holder.itemView.setClickable(true);

        // set background resource (target view ID: container)
        final int expandState = holder.getExpandStateFlags();

        if ((expandState & ExpandableItemConstants.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;
            boolean isExpanded;
            boolean animateIndicator = ((expandState & Expandable.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);

            FrameLayout.LayoutParams params;
            float factor = mFragment.getResources().getDisplayMetrics().density;

            if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                bgResId = R.drawable.bg_list_group_item_expanded_state;
                isExpanded = true;
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        (int ) (80 * factor));
                holder.mDivider.setVisibility(View.VISIBLE);

            } else {
                bgResId = R.drawable.bg_list_group_item_normal_state;
                isExpanded = false;
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        (int ) (90 * factor));
                holder.mDivider.setVisibility(View.GONE);
            }


            holder.mBackground.setLayoutParams(params);
            holder.mContainer.setBackgroundResource(bgResId);
            holder.mIndicator.setExpandedState(isExpanded, animateIndicator);
        }
    }

    @Override
    public void onBindChildViewHolder(final MyChildViewHolder holder, Cursor cursor) {

        holder.mContainer.setOnCreateContextMenuListener(mOnCreateContextMenuListener);

        int cal = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));
        String carbo = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        String prot = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        String fat = String.format(Locale.getDefault(), "%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_FAT)));
        String name = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        if (name.length() > 25) {
            holder.mFoodName.setTextSize(12);
        } else {
            holder.mFoodName.setTextSize(16);
        }

        holder.mFoodName.setText(name);
        holder.mCal.setText(String.valueOf(cal));
        holder.mCarbo.setText(carbo);
        holder.mProt.setText(prot);
        holder.mFat.setText(fat);

        //TODO String with placeholders
        holder.mServing.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_SERVING))
                + mContext.getString(R.string.elv_gram));

        long dateTime = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_RECORD_DATETIME));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(dateTime);

        holder.mTime.setText(new SimpleDateFormat("kk:mm", Locale.getDefault())
                .format(calendar.getTime()));

        int bgResId;
        float factor = mFragment.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams params;

        if (cursor.getPosition() == cursor.getCount() - 1) {
            bgResId = R.drawable.bg_child_item_last;
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (70 *factor));
            holder.mDivider.setVisibility(View.GONE);
        } else {
            bgResId = R.drawable.bg_child_item_normal;
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (64 *factor));
            holder.mDivider.setVisibility(View.VISIBLE);
        }

        holder.mBackground.setLayoutParams(params);
        holder.mContainer.setBackgroundResource(bgResId);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        Loader loader;
        int groupPos = groupCursor.getPosition();
        int groupId = groupCursor.getInt(groupCursor
                .getColumnIndex(DbDiary.ALIAS_ID));

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

    public HashMap<Integer, Integer> getGroupMap () {
        return mGroupMap;
    }

    public void setContextMenuState(boolean state) {
        isContextMenuOpen = state;
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
        // check is enabled
        if (!(holder.itemView.isEnabled() && holder.itemView.isClickable())
                || getChildCount(groupPosition) == 0
                || isContextMenuOpen) {
            return false;
        } else {
            return true;
        }
    }
}
