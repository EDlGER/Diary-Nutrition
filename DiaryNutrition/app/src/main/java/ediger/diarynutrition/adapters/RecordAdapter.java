package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 19.05.15.
 */
public class RecordAdapter extends SimpleCursorAdapter {

    private  Context context;

    public RecordAdapter(Context context, int layout, Cursor c,
                         String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
    }

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm");

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private LayoutInflater layoutInflater;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

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
    public void bindView(View view, Context context, Cursor cursor) {
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
