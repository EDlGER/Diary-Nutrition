package ediger.diarynutrition.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water")
public class Water {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int id;

    private int amount;

    private long datetime;

    public Water(int amount, long datetime) {
        this.amount = amount;
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public long getDatetime() {
        return datetime;
    }
}
