package ediger.diarynutrition.weight;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.AppContext;
import ediger.diarynutrition.PreferenceHelper;
import ediger.diarynutrition.data.DiaryRepository;
import ediger.diarynutrition.data.source.entities.Weight;
import ediger.diarynutrition.objects.SingleLiveEvent;

public class WeightViewModel extends AndroidViewModel {

    private DiaryRepository mRepository;

    private LiveData<List<Weight>> mWeight;

    private MutableLiveData<Calendar> mDateSince = new MutableLiveData<>();

    private LiveData<List<Weight>> mWeightSinceDate;

    public WeightViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((AppContext) application).getRepository();

        mWeight = mRepository.getWeight();

        Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_YEAR, -8);
        mDateSince.setValue(from);

        mWeightSinceDate = Transformations
                .switchMap(mDateSince, date -> mRepository.getWeight(date));
    }

    public LiveData<List<Weight>> getWeight() {
        return mWeight;
    }

    public Weight getRecentWeight() {
        return mRepository.getRecentWeight();
    }

    LiveData<List<Weight>> getWeightSinceDate() {
        return mWeightSinceDate;
    }

    public void addWeight(Weight weight) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(weight.getDatetime());

        Weight lastWeight = mRepository.getWeightForDay(day);

        if (lastWeight == null) {
            mRepository.addWeight(weight);
        } else {
            lastWeight.setAmount(weight.getAmount());
            mRepository.updateWeight(lastWeight);
        }

        //Updates for use of NutritionProgram
        PreferenceHelper.setValue(PreferenceHelper.KEY_WEIGHT, weight.getAmount());
    }

    void deleteWeight(int id) {
        mRepository.deleteWeight(id);
    }

    void setDate(Calendar since) {
        if (since != null) {
            mDateSince.setValue(since);
        }
    }

}
