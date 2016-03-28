package ediger.diarynutrition.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
    private Context mContext;

    protected final HashMap<Integer, Integer> mGroupMap;

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
        } else if ((isExpanded) && (getChildrenCount(groupPosition) != 0)) {
            params.setMargins(pxSide,0,pxSide,pxBottomExp);
            relative.setLayoutParams(params);
            divider.setVisibility(View.VISIBLE);
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

        String carbo = String.format("%.1f",cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        String prot = String.format("%.1f", cursor.getFloat(
                cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        String fat = String.format("%.1f", cursor.getFloat(
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
                + " г");

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
    }
}
