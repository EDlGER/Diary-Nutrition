package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.diary_fragment;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 19.05.15.
 */
public class RecordAdapter extends SimpleCursorTreeAdapter {

   // private Context context;
    private diary_fragment mFragment;
    private MainActivity mActivity;

    protected final HashMap<Integer, Integer> mGroupMap;

    public RecordAdapter(Context context,diary_fragment df, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        //this.context = context;
        mActivity = (MainActivity) context;
        mFragment = df;
        this.layoutInflater = layoutInflater.from(context);
        mGroupMap = new HashMap<Integer, Integer>();
    }

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm");

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private LayoutInflater layoutInflater;

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.record_group_item1,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.meal_name = (TextView) view.findViewById(R.id.txt_meal);

        view.setTag(holder);
        return view;
    }

    @Override
    public View newChildView(Context context, Cursor cursor,boolean isLastChild, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.record_item1,parent,false);

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
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.meal_name.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_M_NAME)));
    }

    @Override
    public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ViewHolder holder = (ViewHolder) view.getTag();


        holder.food_name.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME)));
        holder.cal.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CAL)));
        holder.carbo.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        holder.prot.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        holder.fat.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FAT)));
        holder.serving.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_SERVING))+" гр");

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
        /*int idColumn = groupCursor.getColumnIndex(DbDiary.ALIAS_M_ID);
        Cursor cursor = AppContext.getDbDiary().getDate();
        cursor.moveToFirst();
        long date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATE));
        return AppContext.getDbDiary().getRecordData(date, groupCursor.getInt(idColumn));*/

        int groupPos = groupCursor.getPosition();
        int groupId = groupCursor.getInt(groupCursor.
                getColumnIndex(DbDiary.ALIAS_M_ID));
        mGroupMap.put(groupId, groupPos);

        Loader loader = mFragment.getLoaderManager().getLoader(groupId);
        if (loader != null && !loader.isReset()){
            mFragment.getLoaderManager().restartLoader(groupId,null,mFragment);
        }
        else {
            mFragment.getLoaderManager().initLoader(groupId,null,mFragment);
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

        public TextView meal_name;
    }
}
