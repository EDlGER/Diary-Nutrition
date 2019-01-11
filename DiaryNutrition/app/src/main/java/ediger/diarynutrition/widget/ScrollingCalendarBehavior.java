package ediger.diarynutrition.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollingCalendarBehavior extends AppBarLayout.Behavior {

    public ScrollingCalendarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

//        setDragCallback(new DragCallback() {
//            @Override
//            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
//                return false;
//            }
//        });
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        return false;
    }


}