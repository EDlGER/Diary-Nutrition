package ediger.diarynutrition.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.app.NavUtils;

import com.github.paolorotolo.appintro.AppIntro;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.intro.NutritionSlide;
import ediger.diarynutrition.fragments.intro.PersonSlide;
import ediger.diarynutrition.fragments.intro.PurposeSlide;
import ediger.diarynutrition.fragments.intro.ActivitySlide;

public class IntroActivity extends AppIntro {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setImmersiveMode(true);
        setGoBackLock(true);

        showSkipButton(false);
        setBarColor(ContextCompat.getColor(this,R.color.intro_bar));
        setIndicatorColor(ContextCompat.getColor(this, R.color.intro_dark),
                ContextCompat.getColor(this, R.color.intro_light));
        setColorDoneText(ContextCompat.getColor(this, R.color.intro_dark));
        setImageNextButton(ContextCompat.getDrawable(this, R.drawable.ic_next));

        setDoneText(getString(R.string.intro_done));

        addSlide(new PersonSlide());
        addSlide(new PurposeSlide());
        addSlide(new ActivitySlide());
        addSlide(new NutritionSlide());
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
