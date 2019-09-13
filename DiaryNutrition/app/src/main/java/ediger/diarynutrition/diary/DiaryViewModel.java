package ediger.diarynutrition.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Calendar;
import java.util.List;

import ediger.diarynutrition.R;
import ediger.diarynutrition.data.DiaryRepository;
import ediger.diarynutrition.data.source.entities.MealAndRecords;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.AppContext;
import ediger.diarynutrition.objects.SnackbarMessage;

public class DiaryViewModel extends AndroidViewModel {

    public final LiveData<Water> water;

    private DiaryRepository mRepository;

    private MutableLiveData<Calendar> mDate = new MutableLiveData<>();

    private MediatorLiveData<List<MealAndRecords>> mRecords;

    private LiveData<Summary> mDaySummary;

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    public DiaryViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((AppContext) application).getRepository();
        mDate.setValue(Calendar.getInstance());

        mRecords = new MediatorLiveData<>();
        mRecords.setValue(null);
        mRecords.addSource(mDate, date -> mRecords.setValue(mRepository.getRecords(date)));

        mDaySummary = Transformations.switchMap(mDate, date -> mRepository.getDaySummary(date));
        water = Transformations.switchMap(mDate, date -> mRepository.getWaterSum(date));
    }

    public Calendar getDate() {
        return mDate.getValue();
    }

    public void setDate(Calendar calendar) {
        mDate.setValue(calendar);
    }

    public void addWater(Water water) {
        mRepository.addWater(water);
    }

    LiveData<Summary> getDaySummary() {
        return mDaySummary;
    }

    LiveData<List<MealAndRecords>> getRecords() {
        return mRecords;
    }

    void addRecord(Record record) {
        mRepository.addRecord(record);
    }

    void delRecord(int id) {
        mRepository.delRecord(id);
    }

    void pasteChildren() {
        mDate.setValue(mDate.getValue());
        showSnackbarMessage(R.string.message_record_meal_insert);
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(message);
    }

}
