package ediger.diarynutrition.data;

public class Summary {

    private int cal;
    private int prot;
    private int fat;
    private int carbo;

    private int serving;

    private int waterAmount;

    public Summary(int cal, int prot, int fat, int carbo) {
        this.cal = cal;
        this.prot = prot;
        this.fat = fat;
        this.carbo = carbo;
    }

    public Summary(int cal, int prot, int fat, int carbo, int serving) {
        this.cal = cal;
        this.prot = prot;
        this.fat = fat;
        this.carbo = carbo;
        this.serving = serving;
    }

    public Summary(int waterAmount) {
        this.waterAmount = waterAmount;
    }

    public int getCal() {
        return cal;
    }

    public void setCal(int cal) {
        this.cal = cal;
    }

    public int getProt() {
        return prot;
    }

    public void setProt(int prot) {
        this.prot = prot;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCarbo() {
        return carbo;
    }

    public void setCarbo(int carbo) {
        this.carbo = carbo;
    }

    public int getServing() {
        return serving;
    }

    public void setServing(int serving) {
        this.serving = serving;
    }
}
