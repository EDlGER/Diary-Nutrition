package ediger.diarynutrition.util

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import ediger.diarynutrition.Event

/**
 * Transforms static java function Snackbar.make() to an extension function on View.
 */
fun View.showSnackbar(snackbarText: String, timeLength: Int) {
    Snackbar.make(this, snackbarText, timeLength).run {
        show()
    }
}

/**
 * Triggers a snackbar message when the value contained by snackbarTaskMessageLiveEvent is modified.
 */
fun View.setupSnackbar(
        lifecycleOwner: LifecycleOwner,
        snackbarEvent: LiveData<Event<Int>>,
        timeLength: Int
) {

    snackbarEvent.observe(lifecycleOwner) { event ->
        event.getContentIfNotHandled()?.let {
            showSnackbar(context.getString(it), timeLength)
        }
    }
}

fun View.hitTest(x: Int, y: Int): Boolean {
    val tx = (translationX + 0.5f).toInt()
    val ty = (translationY + 0.5f).toInt()
    val left = left + tx
    val right = right + tx
    val top = top + ty
    val bottom = bottom + ty

    return x in left..right && y >= top && y <= bottom
}