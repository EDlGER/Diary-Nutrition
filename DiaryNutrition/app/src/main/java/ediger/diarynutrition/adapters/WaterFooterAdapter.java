package ediger.diarynutrition.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter;

import ediger.diarynutrition.R;
import ediger.diarynutrition.database.DbDiary;
import ediger.diarynutrition.fragments.SettingsFragment;
import ediger.diarynutrition.objects.AppContext;
import info.abdolahi.CircularMusicProgressBar;

public class WaterFooterAdapter extends AbstractHeaderFooterWrapperAdapter<WaterFooterAdapter.HeaderViewHolder,
        WaterFooterAdapter.FooterViewHolder> {

    private Context mContext;
    private View.OnClickListener mOnClickListener;

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView water;
        TextView waterTotal;
        TextView waterRemain;
        CircularMusicProgressBar pbWater;
        CardView cardView;

        public FooterViewHolder(View itemView) {
            super(itemView);
            water = itemView.findViewById(R.id.txt_water);
            waterTotal = itemView.findViewById(R.id.txt_water_total);
            waterRemain = itemView.findViewById(R.id.txt_water_remain);
            pbWater = itemView.findViewById(R.id.pb_water);
            cardView = itemView.findViewById(R.id.card_water);
        }
    }

    public WaterFooterAdapter(Context context, RecyclerView.Adapter adapter,
                              View.OnClickListener onClickListener) {
        setAdapter(adapter);
        mContext = context;
        mOnClickListener = onClickListener;
    }

    @Override
    public HeaderViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public FooterViewHolder onCreateFooterItemViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_record_footer, parent, false);
        return new FooterViewHolder(v);
    }

    @Override
    public void onBindHeaderItemViewHolder(HeaderViewHolder holder, int localPosition) {

    }

    @Override
    public void onBindFooterItemViewHolder(FooterViewHolder holder, int localPosition) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        int value = 0;
        //TODO change 2000
        int target = pref.getInt(SettingsFragment.KEY_PREF_WATER, 2000);
        holder.waterTotal.setText(String.valueOf(target));

        long date = AppContext.getDate();

        Cursor cursor = AppContext.getDbDiary().getDayWaterData(date);
        if (cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SUM_AMOUNT));
            holder.water.setText(String.valueOf(value));
        }
        cursor.close();
        holder.waterRemain.setText(String.valueOf(target - value));
        holder.pbWater.setValue(value * 100 / target);

        holder.cardView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getFooterItemCount() {
        return 1;
    }
}
