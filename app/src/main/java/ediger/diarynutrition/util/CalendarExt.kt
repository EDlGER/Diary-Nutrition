package ediger.diarynutrition.util

import java.util.*
import java.util.Calendar.*


fun Calendar.getDayBeginning() = (this.clone() as Calendar).apply {
    set(HOUR_OF_DAY, 0)
    set(MINUTE, 0)
    set(SECOND, 0)
    set(MILLISECOND, 0)
}


fun Calendar.getDayEnding() = (this.clone() as Calendar).apply {
    set(HOUR_OF_DAY, 23)
    set(MINUTE, 59)
    set(SECOND, getActualMaximum(SECOND))
    set(MILLISECOND, getActualMaximum(MILLISECOND))
}