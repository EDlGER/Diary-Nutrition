package ediger.diarynutrition.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.intro.FirstSlide;
import ediger.diarynutrition.fragments.intro.SecondSlide;
import ediger.diarynutrition.fragments.intro.ThirdSlide;

/**
 * Created by root on 12.05.16.
 */
public class IntroActivity extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        showSkipButton(false);
        setBarColor(ContextCompat.getColor(this,R.color.intro_bar));
        setIndicatorColor(ContextCompat.getColor(this, R.color.intro_dark),
                ContextCompat.getColor(this, R.color.intro_light));
        setColorDoneText(ContextCompat.getColor(this, R.color.intro_dark));
        setImageNextButton(ContextCompat.getDrawable(this, R.drawable.ic_next));

        setDoneText(getString(R.string.intro_done));

        addSlide(new FirstSlide());
        addSlide(new SecondSlide());
        addSlide(new ThirdSlide());
    }

    @Override
    public void onSkipPressed() {

    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onBackPressed() {

    }
}
