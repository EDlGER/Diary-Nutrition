package ediger.diarynutrition.util;

import java.util.Calendar;

import static ediger.diarynutrition.PreferenceHelper.*;

public class NutritionProgramUtils {

    private static final int [][] PURPOSE_PFC = {
            {30, 30, 40}, //weight loss
            {20, 30, 50}, //preservation
            {30, 20, 50}  //gain
    };
    private static final float [] OFFSET_GENDER = {5,-161};
    private static final double [] OFFSET_ACTIVITY = {1.2, 1.375, 1.55, 1.725};
    private static final int [] OFFSET_PURPOSE = {-400, 0, 400};

    //age, gender, activity, purpose, height, weight -> cal, pfc, pfc%, water
    public static void setToDefault() {

        Calendar birthday = Calendar.getInstance();
        birthday.setTimeInMillis(getValue(KEY_BIRTHDAY, Long.class, 0L));

        int age = Calendar.getInstance().get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        int height = Integer.parseInt(getValue(KEY_HEIGHT, String.class, "0"));
        int gender = Integer.parseInt(getValue(KEY_GENDER, String.class, "1")) - 1;
        int activity = Integer.parseInt(getValue(KEY_ACTIVITY, String.class, "1")) - 1;
        int purpose = Integer.parseInt(getValue(KEY_PURPOSE, String.class, "1")) - 1;

        float weight = getValue(KEY_WEIGHT, Float.class, 0f);

        //Mifflin-St. Jeor Equation
        float cal = (float) (10 * weight + 6.25 * height - 5 * age + OFFSET_GENDER[gender]);
        cal *= OFFSET_ACTIVITY[activity];
        cal += OFFSET_PURPOSE[purpose];

        float prot = PURPOSE_PFC[purpose][0] / 100f * cal / 4f;
        float fat = PURPOSE_PFC[purpose][1] / 100f * cal / 9f;
        float carbo = PURPOSE_PFC[purpose][2] / 100f * cal / 4f;

        int water = gender == 0 ? (int) weight * 35 : (int) weight * 31;

        setValue(KEY_PROGRAM_CAL, cal);
        setValue(KEY_PROGRAM_PROT, prot);
        setValue(KEY_PROGRAM_FAT, fat);
        setValue(KEY_PROGRAM_CARBO, carbo);
        setValue(KEY_PROGRAM_PROT_PERCENT, PURPOSE_PFC[purpose][0]);
        setValue(KEY_PROGRAM_FAT_PERCENT, PURPOSE_PFC[purpose][1]);
        setValue(KEY_PROGRAM_CARBO_PERCENT, PURPOSE_PFC[purpose][2]);
        setValue(KEY_PROGRAM_WATER, water);
    }

    //cal, pfc% -> pfc
    public static void update() {
        float cal = getValue(KEY_PROGRAM_CAL, Float.class, 1.0f);
        float protPct = (float) getValue(KEY_PROGRAM_PROT_PERCENT, Integer.class, 1);
        float fatPct = (float) getValue(KEY_PROGRAM_FAT_PERCENT, Integer.class, 1);
        float carboPct = (float) getValue(KEY_PROGRAM_CARBO_PERCENT, Integer.class, 1);

        setValue(KEY_PROGRAM_PROT, protPct / 100 * cal / 4);
        setValue(KEY_PROGRAM_FAT, fatPct / 100 * cal / 9);
        setValue(KEY_PROGRAM_CARBO, carboPct / 100 * cal / 4);
    }

    public static int getDefaultWater() {
        int gender = Integer.parseInt(getValue(KEY_GENDER, String.class, "1"));
        float weight = getValue(KEY_WEIGHT, Float.class, 0f);

        return gender == 1 ? (int) (weight * 35) : (int) (weight * 31);
    }



}
