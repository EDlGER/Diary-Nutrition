package ediger.diarynutrition.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.widget.CursorAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;

/**
 * Created by ediger on 25.10.17.
 */

abstract class CursorTreeRecyclerAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder>
        extends AbstractExpandableItemAdapter<GVH, CVH> {

    private Context mContext;
    private Handler mHandler;
    private boolean mAutoRequery;

    /** The cursor helper that is used to get the groups */
    MyCursorHelper mGroupCursorHelper;

    /**
     * The map of a group position to the group's children cursor helper (the
     * cursor helper that is used to get the children for that group)
     */
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;

    /**
     * Constructor. The adapter will call {@link Cursor#requery()} on the cursor whenever
     * it changes so that the most recent data is always displayed.
     *
     * @param cursor The cursor from which to get the data for the groups.
     */
    public CursorTreeRecyclerAdapter(Context context, Cursor cursor) {
        setHasStableIds(true);
        init(cursor, context, true);
    }

    /**
     * Constructor.
     *
     * @param cursor The cursor from which to get the data for the groups.
     * @param context The context
     * @param autoRequery If true the adapter will call {@link Cursor#requery()}
     *        on the cursor whenever it changes so the most recent data is
     *        always displayed.
     */
    public CursorTreeRecyclerAdapter(Context context, Cursor cursor, boolean autoRequery) {
        setHasStableIds(true);
        init(cursor, context, autoRequery);
    }

    private void init(Cursor cursor, Context context, boolean autoRequery) {
        mContext = context;
        mHandler = new Handler();
        mAutoRequery = autoRequery;

        mGroupCursorHelper = new MyCursorHelper(cursor);
        mChildrenCursorHelpers = new SparseArray<MyCursorHelper>();
    }

    synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper cursorHelper = mChildrenCursorHelpers.get(groupPosition);

        if (cursorHelper == null) {
            if (mGroupCursorHelper.moveTo(groupPosition) == null) return null;

            final Cursor cursor = getChildrenCursor(mGroupCursorHelper.getCursor());
            cursorHelper = new MyCursorHelper(cursor);
            mChildrenCursorHelpers.put(groupPosition, cursorHelper);
        }

        return cursorHelper;
    }

    /**
     * Gets the Cursor for the children at the given group. Subclasses must
     * implement this method to return the children data for a particular group.
     * <p>
     * If you want to asynchronously query a provider to prevent blocking the
     * UI, it is possible to return null and at a later time call
     * {@link #setChildrenCursor(int, Cursor)}.
     * <p>
     * It is your responsibility to manage this Cursor through the Activity
     * lifecycle. It is a good idea to use {@link Activity#managedQuery} which
     * will handle this for you. In some situations, the adapter will deactivate
     * the Cursor on its own, but this will not always be the case, so please
     * ensure the Cursor is properly managed.
     *
     * @param groupCursor The cursor pointing to the group whose children cursor
     *            should be returned
     * @return The cursor for the children of a particular group, or null.
     */
    abstract protected Cursor getChildrenCursor(Cursor groupCursor);

    /**
     * Sets the group Cursor.
     *
     * @param cursor The Cursor to set for the group. If there is an existing cursor
     * it will be closed.
     */
    public void setGroupCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, false);
    }

    /**
     * Sets the children Cursor for a particular group. If there is an existing cursor
     * it will be closed.
     * <p>
     * This is useful when asynchronously querying to prevent blocking the UI.
     *
     * @param groupPosition The group whose children are being set via this Cursor.
     * @param childrenCursor The Cursor that contains the children of the group.
     */
    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {

        /*
         * Don't request a cursor from the subclass, instead we will be setting
         * the cursor ourselves.
         */
        MyCursorHelper childrenCursorHelper = getChildrenCursorHelper(groupPosition, false);

        /*
         * Don't release any cursor since we know exactly what data is changing
         * (this cursor, which is still valid).
         */
        childrenCursorHelper.changeCursor(childrenCursor, false);
    }

    public Cursor getChild(int groupPosition, int childPosition) {
        // Return this group's children Cursor pointing to the particular child
        return getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).getId(childPosition);
    }

    @Override
    public int getChildCount(int groupPosition) {
        return getChildrenCursorHelper(groupPosition, true).getCount();
    }

    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        return (mGroupCursorHelper.isValid() && helper != null) ? helper.getCount() : 0;
    }

    public Cursor getGroup(int groupPosition) {
        // Return the group Cursor pointing to the given group
        return mGroupCursorHelper.moveTo(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mGroupCursorHelper.getId(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroupCursorHelper.getCount();
    }

    public abstract void onBindGroupViewHolder(GVH holder, Cursor cursor);

    @Override
    public void onBindGroupViewHolder(GVH holder,
                                      int groupPosition,
                                      @IntRange(from = -8388608L, to = 8388607L) int viewType) {
        Cursor cursor = mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        //throw new IllegalStateException("couldn't move cursor to position " + groupPosition);

        onBindGroupViewHolder(holder, cursor);
    }

    public abstract void onBindChildViewHolder(CVH holder, Cursor cursor);

    @Override
    public void onBindChildViewHolder(CVH holder,
                                      int groupPosition,
                                      int childPosition,
                                      @IntRange(from = -8388608L, to = 8388607L) int viewType) {

        Cursor cursor = getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        //throw new IllegalStateException("couldn't move cursor to position " + groupPosition);

        onBindChildViewHolder(holder, cursor);
    }

    private synchronized void releaseCursorHelpers() {
        for (int pos = mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            mChildrenCursorHelpers.valueAt(pos).deactivate();
        }

        mChildrenCursorHelpers.clear();
    }


    /**
     * Notifies a data set change, but with the option of not releasing any
     * cached cursors.
     *
     * @param releaseCursors Whether to release and deactivate any cached
     *            cursors.
     */
    public void notifyDataSetChanged(boolean releaseCursors) {

        if (releaseCursors) {
            releaseCursorHelpers();
        }

        super.notifyDataSetChanged();
    }


    public void notifyDataSetInvalidated() {
        releaseCursorHelpers();
        //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        super.notifyDataSetChanged();
    }

    @Override
    public boolean onHookGroupCollapse(int groupPosition, boolean fromUser) {
        deactivateChildrenCursorHelper(groupPosition);
        return super.onHookGroupCollapse(groupPosition, fromUser);
    }

    /**
     * Deactivates the Cursor and removes the helper from cache.
     *
     * @param groupPosition The group whose children Cursor and helper should be
     *            deactivated.
     */
    synchronized void deactivateChildrenCursorHelper(int groupPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        mChildrenCursorHelpers.remove(groupPosition);
        cursorHelper.deactivate();
    }

    /**
     * @see CursorAdapter#changeCursor(Cursor)
     */
    public void changeCursor(Cursor cursor) {
        mGroupCursorHelper.changeCursor(cursor, true);
    }

    /**
     * @see CursorAdapter#getCursor()
     */
    public Cursor getCursor() {
        return mGroupCursorHelper.getCursor();
    }


    class MyCursorHelper {
        private Cursor mCursor;
        private boolean mDataValid;
        private int mRowIDColumn;
        private MyContentObserver mContentObserver;
        private MyDataSetObserver mDataSetObserver;

        MyCursorHelper(Cursor cursor) {
            final boolean cursorPresent = cursor != null;
            mCursor = cursor;
            mDataValid = cursorPresent;
            mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            mContentObserver = new MyContentObserver();
            mDataSetObserver = new MyDataSetObserver();
            if (cursorPresent) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
            }
        }

        Cursor getCursor() {
            return mCursor;
        }

        int getCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }

        long getId(int position) {
            if (mDataValid && mCursor != null) {
                if (mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

        Cursor moveTo(int position) {
            if (mDataValid && (mCursor != null) && mCursor.moveToPosition(position)) {
                return mCursor;
            } else {
                return null;
            }
        }

        void changeCursor(Cursor cursor, boolean releaseCursors) {
            if (cursor == mCursor) return;

            deactivate();
            mCursor = cursor;
            if (cursor != null) {
                cursor.registerContentObserver(mContentObserver);
                cursor.registerDataSetObserver(mDataSetObserver);
                mRowIDColumn = cursor.getColumnIndex("_id");
                mDataValid = true;
                // notify the observers about the new cursor
                notifyDataSetChanged(releaseCursors);
            } else {
                mRowIDColumn = -1;
                mDataValid = false;
                // notify the observers about the lack of a data set
                notifyDataSetInvalidated();
            }
        }

        void deactivate() {
            if (mCursor == null) {
                return;
            }

            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.unregisterDataSetObserver(mDataSetObserver);
            mCursor.close();
            mCursor = null;
        }

        boolean isValid() {
            return mDataValid && mCursor != null;
        }

        private class MyContentObserver extends ContentObserver {
            public MyContentObserver() {
                super(mHandler);
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
                    if (false) Log.v("Cursor", "Auto requerying " + mCursor +
                            " due to update");
                    mDataValid = mCursor.requery();
                }
            }
        }

        private class MyDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                mDataValid = true;
                //notifyDataSetChanged()
                notifyDataSetChanged(true);
            }

            @Override
            public void onInvalidated() {
                mDataValid = false;
                notifyDataSetInvalidated();
            }
        }
    }
}
