package com.example.capturetranslate;


import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class FrenchToEnglishTranslationTest {

    @Rule
    public IntentsTestRule<TranslateActivity> mIntentsRule = new IntentsTestRule<>(
            TranslateActivity.class);

    @Test
    public void translate_SpanishToEnglish() throws InterruptedException {

        onView( withId( R.id.sourceText ) ).perform( typeText( "Test de langue française" ) );
        onView(withId(R.id.translate)).perform(click());
        Thread.sleep( 1500 );
        onView(withId(R.id.translatedText)).check(matches(withText("French language test")));

    }


}



