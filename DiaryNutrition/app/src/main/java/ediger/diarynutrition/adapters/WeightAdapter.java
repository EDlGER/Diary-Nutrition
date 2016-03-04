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
 * Created by root on 27.02.16.
 */
public class WeightAdapter extends SimpleCursorAdapter{
    private Context mContext;
    private LayoutInflater layoutInflater;

    public WeightAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mContext = context;
        this.layoutInflater = layoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.weight_item1,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.date = (TextView) view.findViewById(R.id.txtDate);
        holder.weight = (TextView) view.findViewById(R.id.txtWeight);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long date = cursor.getLong(cursor.getColumnIndex(DbDiary.ALIAS_DATETIME));
        calendar.setTimeInMillis(date);

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.date.setText(dateFormat.format(calendar.getTime()));
        holder.weight.setText(String.format("%.1f",cursor.getFloat(cursor.getColumnIndex(DbDiary.ALIAS_WEIGHT))));

    }

    private class ViewHolder {
        public TextView date;
        public TextView weight;
    }
}
