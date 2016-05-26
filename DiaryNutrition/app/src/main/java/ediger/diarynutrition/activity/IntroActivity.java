package ediger.diarynutrition.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroViewPager;

import ediger.diarynutrition.R;
import ediger.diarynutrition.fragments.intro.FirstSlide;
import ediger.diarynutrition.fragments.intro.SecondSlide;

/**
 * Created by root on 12.05.16.
 */
public class IntroActivity extends AppIntro {


    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        showSkipButton(false);
        setBarColor(getColor(R.color.intro_bar));
        setIndicatorColor(getColor(R.color.intro_dark),
                getColor(R.color.intro_light));
        setColorDoneText(getColor(R.color.intro_dark));
        setImageNextButton(getDrawable(R.drawable.ic_next_black));

        setDoneText(getString(R.string.intro_done));


        addSlide(new FirstSlide());
        addSlide(new SecondSlide());
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
