package ediger.diarynutrition.data.source.entities;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food")
public class Food {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "food_name")
    private String foodName;

    private float cal;

    private float prot;

    private float fat;

    private float carbo;

    @ColumnInfo(defaultValue = "0")
    private int favorite;

    @ColumnInfo(defaultValue = "0")
    private int user;

    public Food(String foodName, float cal, float prot, float fat, float carbo) {
        this.foodName = foodName;
        this.cal = cal;
        this.prot = prot;
        this.fat = fat;
        this.carbo = carbo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }


    public float getCal() {
        return cal;
    }

    public void setCal(float cal) {
        this.cal = cal;
    }

    public float getProt() {
        return prot;
    }

    public void setProt(float prot) {
        this.prot = prot;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getCarbo() {
        return carbo;
    }

    public void setCarbo(float carbo) {
        this.carbo = carbo;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }
}
