package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckGoButtonClickLiveNavigationTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showGoButtonClickLiveNavigationTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            Thread.sleep(DELAY_2000)

            val sourceEdt = waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click())

            Thread.sleep(DELAY_2000)

            val clMyLocation =
                waitForView(allOf(withText(R.string.label_my_location), isDisplayed()))
            clMyLocation?.perform(click())

            Thread.sleep(DELAY_2000)

            val destinationEdt = waitForView(allOf(withId(R.id.edt_search_dest), isDisplayed()))
            destinationEdt?.perform(click(), replaceText(TEST_WORD_4))

            Thread.sleep(DELAY_2000)

            val suggestionListRv = waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            val btnCarGo = waitForView(
                allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO),
                    ),
                    isDisplayed(),
                ),
            )
            btnCarGo?.perform(click())

            Espresso.closeSoftKeyboard()

            // navListView
            waitForView(allOf(withId(R.id.rv_navigation_list), isDisplayed(), hasMinimumChildCount(1)))

            // btnExit
            waitForView(allOf(withId(R.id.btn_exit), isDisplayed())) {
                failTest(109, null)
            }
        } catch (e: Exception) {
            failTest(145, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
