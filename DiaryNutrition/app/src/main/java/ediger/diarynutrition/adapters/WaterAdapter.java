package ediger.diarynutrition.adapters;

import android.database.Cursor;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.objects.AppContext;

/**
 * Created by ediger on 18.05.17.
 */

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    private long date;
    private Cursor data;
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm", Locale.getDefault());

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relative;

        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            relative = itemView;
        }
    }

    public WaterAdapter(Cursor data, long date) {
        this.data = data;
        this.date = date;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.water_item1, parent, false);
        return new ViewHolder(rl);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RelativeLayout relative = holder.relative;
        TextView txtTime = (TextView) relative.findViewById(R.id.txt_time);
        TextView txtAmount = (TextView) relative.findViewById(R.id.txt_amount);

        FrameLayout frameRemove = (FrameLayout) relative.findViewById(R.id.frame_delete);
        frameRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.moveToPosition(position)) {
                    AppContext.getDbDiary().delWater(data.getLong(data.getColumnIndex(DbDiary.ALIAS_ID)));
                    data = AppContext.getDbDiary().getWaterData(date);
                    notifyItemRemoved(position);
                    notifyItemRangeRemoved(position, getItemCount());
                }
            }
        });

        if (data.moveToPosition(position)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getLong(data.getColumnIndex(DbDiary.ALIAS_DATETIME)));

            txtTime.setText(timeFormatter.format(calendar.getTime()));
            txtAmount.setText(data.getString(data.getColumnIndex(DbDiary.ALIAS_AMOUNT)));
        }
    }

    @Override
    public int getItemCount() {
        return data.getCount();
        /*if (data.moveToFirst()) {
            return data.getCount();
        } else {
            return 0;
        }*/
    }

}
