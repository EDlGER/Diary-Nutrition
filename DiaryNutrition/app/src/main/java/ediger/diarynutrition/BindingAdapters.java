package ediger.diarynutrition;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BindingAdapters {

    @BindingAdapter("timeMillis")
    public static void setTime(TextView view, long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
        view.setText(dateFormat.format(new Date(timeMillis)));
    }

    @BindingAdapter("dateMillis")
    public static void setDate(TextView view, long dateMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        view.setText(dateFormat.format(new Date(dateMillis)));
    }

    @BindingAdapter("customHeight")
    public static void setLayoutHeight(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:textSize")
    public static void setTextSize(View view, int size) {
        ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

}
