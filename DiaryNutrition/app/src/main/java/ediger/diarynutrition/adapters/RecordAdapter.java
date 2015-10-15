package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 19.05.15.
 */
public class RecordAdapter extends SimpleCursorTreeAdapter {

    private  Context context;

    public RecordAdapter(Context context, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo, Context context1) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
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
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        int idColumn = groupCursor.getColumnIndex(DbDiary.ALIAS_M_ID);
        return
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
