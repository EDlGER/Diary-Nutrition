package ediger.diarynutrition.data.source.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water")
public class Water {

    @PrimaryKey(autoGenerate = true)
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

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public long getDatetime() {
        return datetime;
    }
}
