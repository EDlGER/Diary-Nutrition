package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 30.05.15.
 */
public class FoodAdapter extends SimpleCursorAdapter {
    private Context context;

    public FoodAdapter(Context context, int layout, Cursor c,
                         String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
    }


    private LayoutInflater layoutInflater;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.food_item1,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.food_name1 = (TextView) view.findViewById(R.id.txt_f_name);
        holder.carbo1 = (TextView) view.findViewById(R.id.txt_f_carbo);
        holder.prot1 = (TextView) view.findViewById(R.id.txt_f_prot);
        holder.fat1 = (TextView) view.findViewById(R.id.txt_f_fat);
        holder.cal1 = (TextView) view.findViewById(R.id.txt_f_cal);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.food_name1.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_F_NAME)));
        holder.cal1.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_F_CAL)));
        holder.carbo1.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_F_CARBO)));
        holder.prot1.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_F_PROT)));
        holder.fat1.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_F_FAT)));

    }

    private class ViewHolder {
        public TextView food_name1;
        public TextView cal1;
        public TextView carbo1;
        public TextView prot1;
        public TextView fat1;
    }

}
