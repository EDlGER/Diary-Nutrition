package ediger.diarynutrition.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
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
        WaterFooterAdapter.FooterViewHolder>
        implements View.OnClickListener {

    Context mContext;
    View.OnClickListener mOnClickListener;
    long mDate;

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

        public FooterViewHolder(View itemView) {
            super(itemView);
            water = itemView.findViewById(R.id.txt_water);
            waterTotal = itemView.findViewById(R.id.txt_water_total);
            waterRemain = itemView.findViewById(R.id.txt_water_remain);
            pbWater = itemView.findViewById(R.id.pb_water);
        }
    }

    public WaterFooterAdapter(Context context, RecyclerView.Adapter adapter, long date) {
        this(context, adapter, date, null);
    }

    public WaterFooterAdapter(Context context, RecyclerView.Adapter adapter,
                              long date, View.OnClickListener onClickListener) {
        setAdapter(adapter);
        mContext = context;
        mDate = date;
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
                .inflate(R.layout.record_footer, parent, false);
        FooterViewHolder vh = new FooterViewHolder(v);
        vh.itemView.setOnClickListener(this);
        return vh;
    }

    @Override
    public void onBindHeaderItemViewHolder(HeaderViewHolder holder, int localPosition) {

    }

    @Override
    public void onBindFooterItemViewHolder(FooterViewHolder holder, int localPosition) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (pref.getBoolean(SettingsFragment.KEY_PREF_UI_WATER_CARD, true)) {
            holder.itemView.setVisibility(View.VISIBLE);

            int value = 0;
            //2000 заменить
            int target = pref.getInt(SettingsFragment.KEY_PREF_WATER, 2000);
            holder.waterTotal.setText(String.valueOf(target));

            Cursor cursor = AppContext.getDbDiary().getDayWaterData(mDate);
            if (cursor.moveToFirst()) {
                value = cursor.getInt(cursor.getColumnIndex(DbDiary.ALIAS_SUM_AMOUNT));
                holder.water.setText(String.valueOf(value));
            }
            cursor.close();

            holder.waterRemain.setText(String.valueOf(target - value));
            holder.pbWater.setValue(value * 100 / target);
        } else {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    public void updateFooter() {
        getFooterAdapter().notifyDataSetChanged();
    }

    public void updateFooter(long date) {
        mDate = date;
        getFooterAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(view);
        }
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
