package ediger.diarynutrition.data.source.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import ediger.diarynutrition.*
import ediger.diarynutrition.PreferenceHelper.setValue
import kotlin.math.roundToInt
import ediger.diarynutrition.data.source.model.ProgramElement.*

enum class ProgramElement {
    CALORIES, PROT, FAT, CARBO, PROT_PCT, FAT_PCT, CARBO_PCT
}

class Program(
        cal: Float,
        prot: Float,
        fat: Float,
        carbo: Float,
        protPct: Int,
        fatPct: Int,
        carboPct: Int
) : BaseObservable() {

    @get:Bindable
    var cal: Float = cal
        private set(value) {
            field = if (value == 0f) 1f else value
            notifyPropertyChanged(BR.cal)
        }

    @get:Bindable
    var prot: Float = prot
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.prot)
        }

    @get:Bindable
    var fat: Float = fat
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.fat)
        }

    @get:Bindable
    var carbo: Float = carbo
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.carbo)
        }

    @get:Bindable
    var protPct: Int = protPct
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.protPct)
        }

    @get:Bindable
    var fatPct: Int = fatPct
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.fatPct)
        }

    @get:Bindable
    var carboPct: Int = carboPct
        private set(value) {
            field = if (value != field) value else return
            notifyPropertyChanged(BR.carboPct)
        }

    fun savePreference() {
        setValue(KEY_PROGRAM_CAL, cal)
        setValue(KEY_PROGRAM_PROT, prot)
        setValue(KEY_PROGRAM_FAT, fat)
        setValue(KEY_PROGRAM_CARBO, carbo)
        setValue(KEY_PROGRAM_PROT_PERCENT, protPct)
        setValue(KEY_PROGRAM_FAT_PERCENT, fatPct)
        setValue(KEY_PROGRAM_CARBO_PERCENT, carboPct)
    }

    fun setProgramElement(macro: ProgramElement, value: Float) {
        when (macro) {
            PROT -> {
                prot = value
                updatePercentAndCal()
            }
            FAT -> {
                fat = value
                updatePercentAndCal()
            }
            CARBO -> {
                carbo = value
                updatePercentAndCal()
            }
            CALORIES -> {
                cal = value
                updateMacro()
            }
            PROT_PCT -> {
                protPct = value.toInt()
                updateMacro()
            }
            FAT_PCT -> {
                fatPct = value.toInt()
                updateMacro()
            }
            CARBO_PCT -> {
                carboPct = value.toInt()
                updateMacro()
            }
        }
    }

    private fun updateMacro() {
        prot = protPct.toFloat() / 100 * cal / 4
        fat = fatPct.toFloat() / 100 * cal / 9
        carbo = carboPct.toFloat() / 100 * cal / 4
    }

    private fun updatePercentAndCal() {
        cal = 4 * prot + 9 * fat + 4 * carbo

        protPct = (prot * 4 / cal * 100).roundToInt()
        fatPct = (fat * 9 / cal * 100).roundToInt()
        carboPct = (carbo * 4 / cal * 100).roundToInt()
    }

}