package ediger.diarynutrition;

import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.weight.WeightViewModel;

import static ediger.diarynutrition.PreferenceHelper.*;

//TODO Rework
public class NutritionProgramManager {

    private static NutritionProgramManager sInstance;

    private int water;
    private int protPercent;
    private int fatPercent;
    private int carboPercent;
    private float cal;
    private float prot;
    private float fat;
    private float carbo;

    private int [] loss = {30, 30, 40};
    private int [] deduction = {20, 30, 50};
    private int [] gain = {30, 20, 50};
    private int [][] purposePfc = {loss, deduction, gain};

    private NutritionProgramManager() {
    }

    public static NutritionProgramManager getInstance() {
        if (sInstance == null) {
            sInstance = new NutritionProgramManager();
            //calculateProgram();
        }
        return sInstance;
    }

    public void updateProgram() {
        calculateProgram();
        savePreference();
    }

    private static void calculateProgram() {
        float [] genderOffset = {5,-161};
        double [] activityOffset = {1.2, 1.375, 1.55, 1.725};
        int [] purposeOffset = {-400, 0, 400};

        Calendar birthday = Calendar.getInstance();
        birthday.setTimeInMillis(PreferenceHelper.getValue(KEY_BIRTHDAY, Long.class, 0L));

        // TODO: sometimes weight = 0
        float weight;
        WeightViewModel viewModel = new WeightViewModel(AppContext.getInstance());
        Weight recentWeight = viewModel.getRecentWeight();
        weight = recentWeight.getAmount();


        int age = Calendar.getInstance().get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        int height = Integer.parseInt(PreferenceHelper.getValue(KEY_HEIGHT, String.class, "0"));
        int purposeId = Integer.parseInt(PreferenceHelper.getValue(KEY_PURPOSE, String.class, "1")) - 1;
        int gender = Integer.parseInt(PreferenceHelper.getValue(KEY_GENDER, String.class, " 1"));

        if (weight == 0F) {
            weight = PreferenceHelper.getValue(KEY_WEIGHT, Float.class, 0F);
        }

        //Mifflin-St. Jeor Equation
        sInstance.cal = (float) (10 * weight + 6.25 * height - 5 * age +
                genderOffset[Integer.parseInt(PreferenceHelper.getValue(KEY_GENDER, String.class, "1")) - 1]);
        sInstance.cal *= activityOffset[Integer.parseInt(PreferenceHelper.getValue(KEY_ACTIVITY, String.class, "1")) - 1];
        sInstance.cal += purposeOffset[Integer.parseInt(PreferenceHelper.getValue(KEY_PURPOSE, String.class, "1")) - 1];

        sInstance.prot = sInstance.purposePfc[purposeId][0] / 100f * sInstance.cal / 4f;
        sInstance.fat = sInstance.purposePfc[purposeId][1] / 100f * sInstance.cal / 9f;
        sInstance.carbo = sInstance.purposePfc[purposeId][2] / 100f * sInstance.cal / 4f;

        sInstance.protPercent = sInstance.purposePfc[purposeId][0];
        sInstance.fatPercent = sInstance.purposePfc[purposeId][1];
        sInstance.carboPercent = sInstance.purposePfc[purposeId][2];

        sInstance.water = gender == 1 ? (int) weight * 35 : (int) weight * 31;

    }

    private void savePreference() {
        PreferenceHelper.setValue(KEY_PROGRAM_CAL, cal);
        PreferenceHelper.setValue(KEY_PROGRAM_PROT, prot);
        PreferenceHelper.setValue(KEY_PROGRAM_FAT, fat);
        PreferenceHelper.setValue(KEY_PROGRAM_CARBO, carbo);
        PreferenceHelper.setValue(KEY_PROGRAM_PROT_PERCENT, protPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_FAT_PERCENT, fatPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_CARBO_PERCENT, carboPercent);
        PreferenceHelper.setValue(KEY_PROGRAM_WATER, water);
    }

    public int getDefaultWater() {
        int gender = Integer.parseInt(PreferenceHelper.getValue(KEY_GENDER, String.class, "1"));
        float weight = 70;

        WeightViewModel viewModel = new WeightViewModel(AppContext.getInstance());
        List<Weight> weightList = viewModel.getWeight().getValue();
        if (weightList != null && weightList.size() != 0) {
            weight = weightList.get(0).getAmount();
        }

        return gender == 1 ? (int) (weight * 35) : (int) (weight * 31);
    }

    public int getWater() {
        return water;
    }

    public int getProtPercent() {
        return protPercent;
    }

    public int getFatPercent() {
        return fatPercent;
    }

    public int getCarboPercent() {
        return carboPercent;
    }

    public float getCal() {
        return cal;
    }

    public float getProt() {
        return prot;
    }

    public float getFat() {
        return fat;
    }

    public float getCarbo() {
        return carbo;
    }
}
