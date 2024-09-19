package ediger.diarynutrition.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

fun Activity.hideKeyboard(view: View?) = view?.let {
    (getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Activity.showKeyboard(view: View? = null) = view?.let {
    if (it.requestFocus()) {
        (getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
