package ediger.diarynutrition.diary.water;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.data.DiaryRepository;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.AppContext;

public class WaterViewModel extends AndroidViewModel {

    private final DiaryRepository mRepository;

    private final MutableLiveData<Calendar> mDate = new MutableLiveData<>();

    private final LiveData<List<Water>> mWater;

    public WaterViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((AppContext) application).getRepository();
        mDate.setValue(Calendar.getInstance());

        mWater = Transformations.switchMap(mDate, mRepository::getWaterList);
    }

    public void setDate(Calendar day) {
        if (day != null) {
            mDate.setValue(day);
        }
    }

    public LiveData<List<Water>> getWater() {
        return mWater;
    }

    public void deleteWater(int id) {
        mRepository.deleteWater(id);
    }
}
