package ediger.diarynutrition.data.source.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal")
public class Meal {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    public Meal(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
