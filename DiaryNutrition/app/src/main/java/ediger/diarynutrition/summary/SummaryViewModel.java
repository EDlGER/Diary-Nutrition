package ediger.diarynutrition.summary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ediger.diarynutrition.AppContext;
import ediger.diarynutrition.data.DiaryRepository;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.entities.Water;

public class SummaryViewModel extends AndroidViewModel {

    private int mNumOfDays = 1;

    private MutableLiveData<Calendar> mSinceDate = new MutableLiveData<>();

    private MediatorLiveData<List<Summary>> mSummary = new MediatorLiveData<>();

    private MediatorLiveData<List<Water>> mWater = new MediatorLiveData<>();

    private DiaryRepository mRepository;

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((AppContext) application).getRepository();

        mSummary.setValue(Collections.emptyList());
        mSummary.addSource(mSinceDate, date -> mSummary.setValue(mRepository.getSummary(date, mNumOfDays)));

        mWater.setValue(Collections.emptyList());
        mWater.addSource(mSinceDate, date -> mWater.setValue(mRepository.getWaterSummary(date, mNumOfDays)));
    }

    public LiveData<List<Summary>> getSummary() {
        return mSummary;
    }

    public LiveData<List<Water>> getWater() {
        return mWater;
    }

    public void setPeriod(Calendar since, int numberOfDays) {
        mNumOfDays = numberOfDays;
        mSinceDate.setValue(since);
    }
}
