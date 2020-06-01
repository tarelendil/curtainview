package com.stas.android.curtainviewsample.activities

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stas.android.curtainviewsample.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurtainActivityTest {

    private fun launchTestActivity() {
        launch(CurtainActivity::class.java).apply {
            onActivity { activity ->

            }
        }
    }

    @Test
    fun activityWithCurtainContainer_checkSwipeDownTest() =
        runBlockingTest {
            launchTestActivity()
            onView(withId(R.id.container_view)).let { containerView ->
                onView(withId(R.id.curtain_view)).let { curtainView ->
                    curtainView.check(matches(not(isDisplayed())))
                    curtainView.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
                    containerView.perform(swipeDown())
                    delay(1000)
                    curtainView.check(matches(isCompletelyDisplayed()))
                    delay(2000)
                }
            }
        }


    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }
}