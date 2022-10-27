package ediger.diarynutrition.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val FIRST_RUN = booleanPreferencesKey("first_run")

        val GENDER = stringPreferencesKey("gender")
        val BIRTHDAY = longPreferencesKey("birthday")
        val HEIGHT = stringPreferencesKey("height")
        val PURPOSE = stringPreferencesKey("purpose")
        val ACTIVITY = stringPreferencesKey("activity")
        val WEIGHT = floatPreferencesKey("weight")

        val PROGRAM_CAL = floatPreferencesKey("program_calories")
        val PROGRAM_PROT = floatPreferencesKey("program_prot")
        val PROGRAM_FAT = floatPreferencesKey("program_fat")
        val PROGRAM_CARBO = floatPreferencesKey("program_carbo")
        val PROGRAM_PROT_PERCENT = intPreferencesKey("prot_perc")
        val PROGRAM_FAT_PERCENT = intPreferencesKey("fat_perc")
        val PROGRAM_CARBO_PERCENT = intPreferencesKey("carbo_perc")
        val PROGRAM_WATER = intPreferencesKey("water")

        // TODO: init language
        val LANGUAGE = stringPreferencesKey("language")
        val LANGUAGE_DB = stringPreferencesKey("language_db")
        val PREF_UI_DEFAULT_TAB = stringPreferencesKey("default_tab")
        val MEAL_ORDER = stringPreferencesKey("meal_order")
        val MEAL_HIDDEN = stringPreferencesKey("hidden_meals")

        val ONLINE_DB_VERSION = intPreferencesKey("online_db_version")
        val LOCAL_DB_VERSION = intPreferencesKey("local_db_version")

        val UI_WATER_CARD = booleanPreferencesKey("water_card")
        val WATER_SERVING_1 = intPreferencesKey("water_serving_1")
        val WATER_SERVING_2 = intPreferencesKey("water_serving_2")
        val WATER_SERVING_3 = intPreferencesKey("water_serving_3")
        val WATER_SERVING_4 = intPreferencesKey("water_serving_4")

        val PREF_PREMIUM = booleanPreferencesKey("premium")
        val USER_FOOD_CONSTRAINT = intPreferencesKey("user_food_constraint")
    }

}