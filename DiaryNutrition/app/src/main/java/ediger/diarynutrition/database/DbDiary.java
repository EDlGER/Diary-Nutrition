package ediger.diarynutrition.database;

import android.database.Cursor;

//TODO MOCK, remove
public class DbDiary {
    public static final String ALIAS_DATETIME = "";
    public static final String ALIAS_WEIGHT = "";
    public static final String ALIAS_FAV = "";
    public static final String ALIAS_FOOD_NAME = "";
    public static final String ALIAS_CAL = "";
    public static final String ALIAS_CARBO = "";
    public static final String ALIAS_PROT = "";
    public static final String ALIAS_FAT = "";
    public static final String ALIAS_FOOD_ID = "";
    public static final String ALIAS_SERVING = "";
    public static final String ALIAS_RECORD_DATETIME = "";
    public static final String ALIAS_MEAL_ID = "";
    public static final String ALIAS_SUM_AMOUNT = "";
    public static final String ALIAS_ID = "";
    public static final String ALIAS_AMOUNT = "";
    public static final String ALIAS_M_NAME = "";

    public Cursor getRecordById(long recordId) {
        return null;
    }

    public Cursor getNameFood(long foodId) {
        return null;
    }

    public void addRec(long foodId, int i, long date, int mealId) {
    }

    public void editRec(long recordId, long foodId, int i, long date, int mealId) {
    }

    public Cursor getFood(long foodId) {
        return null;
    }

    public Cursor getAllWeight() {
        return null;
    }

    public Cursor getDayData(long date) {
        return null;
    }

    public Cursor getDayWaterData(long date) {
        return null;
    }

    public Cursor getFavorFood() {
        return null;
    }

    public void setFavor(long id, int i) {
    }

    public String[] getFilterFood() {
        return null;
    }

    public String[] getListFood() {
        return null;
    }

    public Cursor getUserFood() {
        return null;
    }

    public void delFood(long id) {
    }

    public String[] getListWeight() {
        return null;
    }

    public void delWeight(long id) {
    }

    public Cursor getWeightFrom(long l) {
        return null;
    }

    public Cursor getWaterData(long date) {
        return null;
    }

    public void delWater(long l) {

    }

    public Cursor getMealData() {
        return null;
    }

    public void delRec(long id) {
    }

    public Cursor getRecordData(long date, int groupId) {
        return null;
    }

    public Cursor getAllFood() {
        return null;
    }

    public String getDbName() {
        return null;
    }

    public void backupDb() {
    }

    public void restoreDb() {
    }

    public void addWater(long time, int parseInt) {
    }

    public Cursor getWeight(long date) {
        return null;
    }

    public void setWeight(long date, float parseFloat) {
    }

    public void addWeight(long date, float parseFloat) {
    }

    public void editFood(long food_id, String toString, float parseFloat, float parseFloat1, float parseFloat2, float parseFloat3) {
    }

    public Cursor getGroupData(long date, int groupId) {
        return null;
    }

    public void addFood(String toString, float parseFloat, float parseFloat1, float parseFloat2, float parseFloat3) {
    }
}
