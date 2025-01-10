package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DISTANCE_EMPTY
import com.aws.amazonlocation.TEST_WORD_TALWALKERS_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_ALLOW
import com.aws.amazonlocation.WHILE_USING_THE_APP_CAPS
import com.aws.amazonlocation.actions.swipeLeft
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchDistanceTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testSearchDistanceTest() {
        try {
            val btnContinueToApp =
                uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
            }
            uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
            uiDevice.findObject(By.text(ALLOW))?.click()
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            onView(withId(R.id.mapView)).perform(swipeLeft())

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))

            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(
                replaceText(
                    TEST_WORD_TALWALKERS_SHYAMAL_CROSS_ROAD,
                ),
            )
            Thread.sleep(DELAY_15000)
            onView(withId(R.id.rv_search_places_suggestion)).check(
                matches(
                    hasMinimumChildCount(1),
                ),
            )
            val tvDistance = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/tv_distance"))
            val distanceValue = tvDistance.text.split(" ")[0].toDouble()
            Assert.assertTrue(
                TEST_FAILED_DISTANCE_EMPTY,
                distanceValue > 0.0
            )
        } catch (e: Exception) {
            failTest(71, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
