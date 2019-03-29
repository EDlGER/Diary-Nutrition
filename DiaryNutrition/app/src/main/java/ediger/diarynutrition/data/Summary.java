package ediger.diarynutrition.data;

public class Summary {

    private float cal;
    private float prot;
    private float fat;
    private float carbo;

    public Summary(float cal, float prot, float fat, float carbo) {
        this.cal = cal;
        this.prot = prot;
        this.fat = fat;
        this.carbo = carbo;
    }

    public float getCal() {
        return cal;
    }

    public void setCal(int cal) {
        this.cal = cal;
    }

    public float getProt() {
        return prot;
    }

    public void setProt(int prot) {
        this.prot = prot;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public float getCarbo() {
        return carbo;
    }

    public void setCarbo(int carbo) {
        this.carbo = carbo;
    }
}
