package ediger.diarynutrition.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;

/**
 * Created by root on 30.05.15.
 */
public class FoodAdapter extends SimpleCursorAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    public FoodAdapter(Context context, int layout, Cursor c,
                         String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.food_item1,parent,false);

        ViewHolder holder = new ViewHolder();
        holder.food_name = (TextView) view.findViewById(R.id.txt_f_name);
        holder.carbo = (TextView) view.findViewById(R.id.txt_f_carbo);
        holder.prot = (TextView) view.findViewById(R.id.txt_f_prot);
        holder.fat = (TextView) view.findViewById(R.id.txt_f_fat);
        holder.cal = (TextView) view.findViewById(R.id.txt_f_cal);
        holder.favor = (ImageView) view.findViewById(R.id.star_view);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        int fav = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_FAV));

        String name = cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FOOD_NAME));
        if (name.length() > 25) {
            holder.food_name.setTextSize(12);
        } else {
            holder.food_name.setTextSize(16);
        }

        if (fav == 1) {
            holder.favor.setVisibility(View.VISIBLE);
        } else {
            holder.favor.setVisibility(View.INVISIBLE);
        }

        holder.food_name.setText(name);
        holder.cal.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CAL)));
        holder.carbo.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_CARBO)));
        holder.prot.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_PROT)));
        holder.fat.setText(cursor.getString(cursor.getColumnIndex(DbDiary.ALIAS_FAT)));

    }

    private class ViewHolder {
        public TextView food_name;
        public TextView cal;
        public TextView carbo;
        public TextView prot;
        public TextView fat;
        public ImageView favor;
    }

}
