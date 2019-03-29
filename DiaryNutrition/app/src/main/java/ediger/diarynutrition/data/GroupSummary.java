package ediger.diarynutrition.data;

import androidx.room.Embedded;

public class GroupSummary {

    @Embedded
    public Summary summary;

    private int serving;

    public GroupSummary(int serving) {
        this.serving = serving;
    }

    public int getServing() {
        return serving;
    }

    public void setServing(int serving) {
        this.serving = serving;
    }
}
