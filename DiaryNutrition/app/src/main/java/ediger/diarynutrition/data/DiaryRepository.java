package ediger.diarynutrition.data;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import ediger.diarynutrition.AppExecutors;
import ediger.diarynutrition.data.source.DiaryDatabase;
import ediger.diarynutrition.data.source.dao.RecordDao;
import ediger.diarynutrition.data.source.dao.SummaryDao;
import ediger.diarynutrition.data.source.dao.WaterDao;
import ediger.diarynutrition.data.source.dao.WeightDao;
import ediger.diarynutrition.data.source.entities.MealAndRecords;
import ediger.diarynutrition.data.source.entities.Record;
import ediger.diarynutrition.data.source.entities.Summary;
import ediger.diarynutrition.data.source.entities.Water;
import ediger.diarynutrition.data.source.entities.Weight;

public class DiaryRepository {

    private static DiaryRepository sInstance;

    private final DiaryDatabase mDatabase;
    private AppExecutors mAppExecutors;

    private DiaryRepository(final DiaryDatabase database, @NonNull AppExecutors appExecutors) {
        mDatabase = database;
        mAppExecutors = appExecutors;
    }

    public static DiaryRepository getInstance(final DiaryDatabase database,
                                              @NonNull AppExecutors appExecutors) {
        if (sInstance == null) {
            synchronized (DiaryRepository.class) {
                if (sInstance == null) {
                    sInstance = new DiaryRepository(database, appExecutors);
                }
            }
        }
        return sInstance;
    }

    //TODO Check correctness
    public List<MealAndRecords> getRecords(Calendar day) {
        List<MealAndRecords> result = null;
        long from = getDayBeginning(day);
        long to = getDayEnding(day);

        try {
            result = new GetAsyncTask(mDatabase.recordDao()).execute(from, to).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

//    public void addRecord(final Record record) {
//        mAppExecutors.discIO().execute(() -> mDatabase.recordDao().insertRecord(record));
//    }

//    public void delRecord(int id) {
//        mAppExecutors.discIO().execute(() -> mDatabase.recordDao().delRecordById(id));
//    }

    public LiveData<Summary> getDaySummary(Calendar day) {
        long from = getDayBeginning(day);
        long to = getDayEnding(day);
        return mDatabase.summaryDao().getDaySummary(from, to);
    }

    public List<Summary> getSummary(Calendar since, int numberOfDays) {
        List<Summary> result = new ArrayList<>();

        Calendar before = (Calendar) since.clone();
        before.add(Calendar.DAY_OF_YEAR, numberOfDays - 1);
        long from = getDayBeginning(since);
        long to = getDayEnding(before);

        try {
            result = new GetSummaryAsyncTask(mDatabase.summaryDao()).execute(from, to).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Water> getWaterSummary(Calendar since, int numberOfDays) {
        List<Water> result = new ArrayList<>();

        Calendar before = (Calendar) since.clone();
        before.add(Calendar.DAY_OF_YEAR, numberOfDays - 1);
        long from = getDayBeginning(since);
        long to = getDayEnding(before);

        try {
            result = new GetWaterSummaryAsyncTask(mDatabase.waterDao()).execute(from, to).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public LiveData<List<Water>> getWaterList(Calendar day) {
        long from = getDayBeginning(day);
        long to = getDayEnding(day);
        return mDatabase.waterDao().getWaterList(from, to);
    }

    public LiveData<Water> getWaterSum(Calendar day) {
        long from = getDayBeginning(day);
        long to = getDayEnding(day);
        return mDatabase.waterDao().getWaterSum(from, to);
    }

    public void addWater(final Water water) {
        mAppExecutors.discIO().execute(() -> mDatabase.waterDao().insertWater(water));
    }

    public void deleteWater(int id) {
        mAppExecutors.discIO().execute(() -> mDatabase.waterDao().deleteWaterById(id));
    }

    public LiveData<List<Weight>> getWeight() {
        return mDatabase.weightDao().loadWeight();
    }

    public LiveData<List<Weight>> getWeight(Calendar since) {
        return mDatabase.weightDao()
                .getWeight(since.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
    }

    public Weight getRecentWeight() {
        final Weight weight = new Weight(0, 0);
        mAppExecutors.discIO().execute(() -> {
            weight.setAmount(mDatabase.weightDao().lastWeight().getAmount());
            weight.setDatetime(mDatabase.weightDao().lastWeight().getDatetime());
        });
        return weight;
    }

    @Nullable
    public Weight getWeightForDay(Calendar day) {
        long from = getDayBeginning(day);
        long to = getDayEnding(day);
        Weight weight = null;
        try {
            weight = new GetWeightAsyncTask(mDatabase.weightDao()).execute(from, to).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return weight;
    }

    public void addWeight(final Weight weight) {
        mAppExecutors.discIO().execute(() -> mDatabase.weightDao().insertWeight(weight));
    }

    public void updateWeight(final Weight weight) {
        mAppExecutors.discIO().execute(() -> mDatabase.weightDao().updateWeight(weight));
    }

    public void deleteWeight(int id) {
        mAppExecutors.discIO().execute(() -> mDatabase.weightDao().deleteWeightById(id));
    }

    private long getDayBeginning(Calendar calendar) {
        Calendar day = (Calendar) calendar.clone();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        return day.getTimeInMillis();
    }

    private long getDayEnding(Calendar calendar) {
        Calendar day = (Calendar) calendar.clone();
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, day.getActualMaximum(Calendar.SECOND));
        day.set(Calendar.MILLISECOND, day.getActualMaximum(Calendar.MILLISECOND));
        return day.getTimeInMillis();
    }

    private static class GetAsyncTask extends AsyncTask<Long, Void, List<MealAndRecords>> {

        private RecordDao mAsyncRecordDao;

        GetAsyncTask(RecordDao recordDao) {
            mAsyncRecordDao = recordDao;
        }

        @Override
        protected List<MealAndRecords> doInBackground(Long... longs) {
            return mAsyncRecordDao.getMealsAndRecords(longs[0], longs[1]);
        }
    }

    private static class GetWeightAsyncTask extends AsyncTask<Long, Void, Weight> {

        private WeightDao mAsyncWeightDao;

        GetWeightAsyncTask(WeightDao weightDao) {
            mAsyncWeightDao = weightDao;
        }

        @Override
        protected Weight doInBackground(Long... longs) {
            return mAsyncWeightDao.getWeightForDay(longs[0], longs[1]);
        }
    }

    private static class GetSummaryAsyncTask extends AsyncTask<Long, Void, List<Summary>> {

        private SummaryDao mAsyncSummaryDao;

        GetSummaryAsyncTask(SummaryDao summaryDao) {
            mAsyncSummaryDao = summaryDao;
        }

        @Override
        protected List<Summary> doInBackground(Long... longs) {
            return mAsyncSummaryDao.getMacroSummary(longs[0], longs[1]);
        }
    }

    private static class GetWaterSummaryAsyncTask extends AsyncTask<Long, Void, List<Water>> {

        private WaterDao mAsyncWaterDao;

        GetWaterSummaryAsyncTask(WaterDao waterDao) {
            mAsyncWaterDao = waterDao;
        }

        @Override
        protected List<Water> doInBackground(Long... longs) {
            return mAsyncWaterDao.getWaterSummary(longs[0], longs[1]);
        }
    }

}
