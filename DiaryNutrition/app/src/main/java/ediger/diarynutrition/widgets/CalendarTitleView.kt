package ediger.diarynutrition.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import ediger.diarynutrition.R
import ediger.diarynutrition.databinding.ToolbarCalendarTitleBinding
import org.apache.commons.lang3.time.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

class CalendarTitleView(context: Context) : FrameLayout(context) {

    private val binding = ToolbarCalendarTitleBinding.inflate(
            LayoutInflater.from(context), this, true
    )

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    private val today: Calendar = getInstance()

    private var currentArrowRotation = 360.0f

    fun animateArrow() {
        val anim = RotateAnimation(currentArrowRotation,
                currentArrowRotation + 180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f).apply {
            interpolator = LinearInterpolator()
            fillAfter = true
            isFillEnabled = true
            duration = 300
        }

        currentArrowRotation = (currentArrowRotation + 180.0f) % 360.0f

        binding.imgArrow.startAnimation(anim)
    }

    fun datePickerClick() { binding.datePickerButton.performClick() }

    fun setDateClickListener(listener: OnClickListener) {
        binding.datePickerButton.setOnClickListener(listener)
    }

    fun setSubtitle(date: Calendar) {
        val yesterday = (today.clone() as Calendar).apply { add(DAY_OF_YEAR, -1) }
        val tomorrow = (today.clone() as Calendar).apply { add(DAY_OF_YEAR, 1) }

        when {
            DateUtils.isSameDay(date, today) -> binding.txtSubtitle.setText(R.string.diary_date_today)

            DateUtils.isSameDay(date, yesterday) -> binding.txtSubtitle.setText(R.string.diary_date_yesterday)

            DateUtils.isSameDay(date, tomorrow) -> binding.txtSubtitle.setText(R.string.diary_date_tomorrow)

            else -> binding.txtSubtitle.text = dateFormat.format(date.time)
        }
    }
}
