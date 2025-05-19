package com.aws.amazonlocation.ui.main

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.allowNotificationPermission
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.performBackPress
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SimulationBackPressTest : BaseTestMainActivity() {
    @Test
    fun simulationBackPressTest() {
        try {
            checkLocationPermission()
            val tracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                        isDisplayed()
                    )
                )
            tracking?.perform(click())

            val btnTryTracker =
                waitForView(
                    allOf(
                        withId(R.id.btn_start_simulation),
                        isDisplayed()
                    )
                )
            btnTryTracker?.perform(click())

            allowNotificationPermission()

            performBackPress()

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.exit)),
                    isDisplayed()
                )
            )?.perform(click())

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.menu_navigate)),
                    isDisplayed()
                )
            )
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
