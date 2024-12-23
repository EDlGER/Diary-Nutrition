package ediger.diarynutrition;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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

    @BindingAdapter("show")
    public static void show(View view, boolean shouldShow) {
        if (view.getVisibility() == View.VISIBLE && shouldShow
                || view.getVisibility() == View.INVISIBLE && !shouldShow) {
            return;
        }
        Animation animation;
        if (shouldShow) {
            view.setVisibility(View.VISIBLE);
            animation = new TranslateAnimation(0, 0, 300, 0);
            animation.setDuration(750);
            animation.setFillAfter(true);
            view.startAnimation(animation);
        } else {
            animation = new TranslateAnimation(0, 0, 0, 300);
            animation.setDuration(750);
            animation.setFillAfter(true);
            view.startAnimation(animation);
            view.setVisibility(View.INVISIBLE);
        }
    }
}
