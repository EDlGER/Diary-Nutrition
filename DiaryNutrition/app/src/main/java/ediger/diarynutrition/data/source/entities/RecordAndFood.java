package ediger.diarynutrition.data.source.entities;

import androidx.room.Embedded;

public class RecordAndFood {

    @Embedded
    public Record record;

    @Embedded(prefix = "f_")
    public Food food;

}
