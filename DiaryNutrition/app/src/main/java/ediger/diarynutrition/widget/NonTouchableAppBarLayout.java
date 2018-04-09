package ediger.diarynutrition.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ediger on 25.03.18.
 */

public class NonTouchableAppBarLayout extends AppBarLayout {
    public NonTouchableAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
