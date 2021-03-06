package com.stas.android.curtainviewsample.activities

import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.*
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stas.android.curtainviewsample.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurtainActivityTest {

    @Before
    fun setUp() {
        launchTestActivity()
    }


    private fun launchTestActivity() {
        launch<CurtainActivity>(
            Intent(
                ApplicationProvider.getApplicationContext(),
                CurtainActivity::class.java
            ).putExtra(CurtainActivity.WITH_ACTION_BAR, true)
        ).apply {
            onActivity { activity ->
            }
        }
    }

    @Test
    fun activityWithCurtainContainer_checkSwipeDown() {
        runBlockingTest {
            onView(withId(R.id.container_view)).let { containerView ->
                onView(withId(R.id.curtain_view)).let { curtainView ->
                    curtainView.check(matches(not(isDisplayed())))
                    curtainView.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
                    containerView.perform(swipeDown())
                    delay(1000)
                    curtainView.check(matches(isCompletelyDisplayed()))
                }
            }
        }
    }

    @Test
    fun activityWithCurtainContainer_checkSwipeUp() =
        runBlockingTest {
            onView(withId(R.id.container_view)).let { containerView ->
                onView(withId(R.id.curtain_view)).let { curtainView ->
                    curtainView.check(matches(not(isDisplayed())))
                    curtainView.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
                    containerView.perform(swipeUp())
                    delay(2000)
                    curtainView.check(matches(not(isDisplayed())))
                }
            }
        }

    @Test
    fun activityWithCurtainContainer_checkSwipeDownAndSwipeUp() {
        runBlockingTest {
            onView(withId(R.id.container_view)).let { containerView ->
                onView(withId(R.id.curtain_view)).let { curtainView ->
                    curtainView.check(matches(not(isDisplayed())))
                    curtainView.check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
                    containerView.perform(swipeDown())
                    delay(2000)
                    curtainView.check(matches(isCompletelyDisplayed()))
                    containerView.perform(swipeUp())
                    delay(2000)
                    curtainView.check(matches(not(isDisplayed())))
                }
            }
        }
    }


    @After
    fun tearDown() {
    }
}