package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DISTANCE_EMPTY
import com.aws.amazonlocation.TEST_WORD_TALWALKERS_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.actions.swipeLeft
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchDistanceTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testSearchDistanceTest() {
        try {
            checkLocationPermission()

            onView(withId(R.id.mapView)).perform(swipeLeft())

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))

            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(
                replaceText(
                    TEST_WORD_TALWALKERS_SHYAMAL_CROSS_ROAD,
                ),
            )
            waitForView(allOf(withId(R.id.rv_search_places_suggestion), isDisplayed(), hasMinimumChildCount(1)))

            val tvDistance = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/tv_distance"))
            val distanceValue = tvDistance.text.split(" ")[0].toDouble()
            Assert.assertTrue(
                TEST_FAILED_DISTANCE_EMPTY,
                distanceValue > 0.0
            )
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
