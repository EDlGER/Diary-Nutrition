package ediger.diarynutrition.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroViewPager;

import ediger.diarynutrition.fragments.intro.FirstSlide;
import ediger.diarynutrition.fragments.intro.SecondSlide;

/**
 * Created by root on 12.05.16.
 */
public class IntroActivity extends AppIntro2 {


    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        addSlide(new FirstSlide());
        addSlide(new SecondSlide());
    }


    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onBackPressed() {

    }
}
