package ediger.diarynutrition.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import ediger.diarynutrition.MainActivity;
import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.DiaryFragment;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by root on 19.05.15.
 */
public class RecordAdapter extends SimpleCursorTreeAdapter {

    private DiaryFragment mFragment;
    private MainActivity mActivity;

    protected final HashMap<Integer, Integer> mGroupMap;

    public RecordAdapter(Context context,DiaryFragment df, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
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

        if (view == null) {
            view = layoutInflater.inflate(R.layout.record_group_item1,parent,false);
        }

        TextView group_name = (TextView) view.findViewById(R.id.txt_meal);
        TextView group_cal = (TextView) view.findViewById(R.id.txtGroupCal);
        TextView group_carbo = (TextView) view.findViewById(R.id.txtGroupCarbo);
        TextView group_prot = (TextView) view.findViewById(R.id.txtGroupProt);
        TextView group_fat = (TextView) view.findViewById(R.id.txtGroupFat);

        Cursor c = AppContext.getDbDiary().getMealData();
        c.moveToPosition(groupPosition);

        group_name.setText(c.getString(c.getColumnIndex(DbDiary.ALIAS_M_NAME)));

        c = AppContext.getDbDiary().getGroupData(date);
        int mealID;

        group_cal.setText("");
        group_carbo.setText("");
        group_prot.setText("");
        group_fat.setText("");

        if (c.moveToFirst()) {
            do {
                mealID = c.getInt(0);
                if ( mealID == groupPosition+1){
                    cal = (int) c.getFloat(c.getColumnIndex(DbDiary.ALIAS_CAL));

                    group_cal.setText(Integer.toString(cal));

                    String carbo = String.format("%.1f",c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_CARBO)));
                    String prot = String.format("%.1f", c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_PROT)));
                    String fat = String.format("%.1f", c.getFloat(
                            c.getColumnIndex(DbDiary.ALIAS_FAT)));


                    group_carbo.setText(carbo);
                    group_prot.setText(prot);
                    group_fat.setText(fat);
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
        holder.shadow = view.findViewById(R.id.shadow);

        view.setTag(holder);

        return view;
    }



    @Override
    public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (isLastChild) {
            holder.shadow.setVisibility(View.VISIBLE);
        } else {
            holder.shadow.setVisibility(View.INVISIBLE);
        }

        //Калории без дробной части
        int cal = (int) cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_CAL));

        String carbo = String.format("%.1f",cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        String prot = String.format("%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        String fat = String.format("%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_FAT)));

        holder.food_name.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME)));
        holder.cal.setText(Integer.toString(cal));
        holder.carbo.setText(carbo);
        holder.prot.setText(prot);
        holder.fat.setText(fat);

        holder.serving.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_SERVING))
                + " гр.");

        //holder.carbo.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        //holder.prot.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        //holder.fat.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FAT)));


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
        public View shadow;
    }
}
