package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.waitForView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SimulationStartTrackingHistoryLoggedTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showStartTrackingHistoryLoggedTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            val tracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
                DELAY_1000
            )

            val clEnableTracking =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_enable_tracking)
            if (clEnableTracking.visibility == View.VISIBLE) {
                val btnTryTracker =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_try_tracker)
                mActivityRule.activity.runOnUiThread {
                    btnTryTracker.performClick()
                }
            }
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_simulation))),
                DELAY_1000
            )

            val labelStartSimulation =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_simulation)))
            labelStartSimulation.click()
            swipeUp()

            val rvTrackingSimulation =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_tracking_simulation)
            val itemCount = rvTrackingSimulation.adapter?.itemCount ?: 0

            val ivBackArrowChangeRoute =
                mActivityRule.activity.findViewById<AppCompatImageView>(R.id.iv_back_arrow_change_route)
            mActivityRule.activity.runOnUiThread {
                ivBackArrowChangeRoute.performClick()
            }
            waitForView(allOf(withId(R.id.rv_tracking_simulation), isDisplayed(), hasMinimumChildCount(1)))
            if (rvTrackingSimulation.adapter?.itemCount != null) {
                rvTrackingSimulation.adapter?.itemCount?.let {
                    Assert.assertTrue(TEST_FAILED_NO_TRACKING_HISTORY, it > itemCount)
                }
            } else {
                Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
            }
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun swipeUp(): UiDevice? {
        // Get the screen dimensions
        val screenHeight = getInstrumentation().targetContext.resources.displayMetrics.heightPixels

        // Set the starting point for the swipe (bottom-center of the screen)
        val startX = getInstrumentation().targetContext.resources.displayMetrics.widthPixels / 2f
        val startY = screenHeight - 100 // Offset from the bottom of the screen

        // Set the ending point for the swipe (top-center of the screen)
        val endY = 100 // Offset from the top of the screen

        // Perform the swipe action
        val uiDevice = UiDevice.getInstance(getInstrumentation())
        uiDevice.swipe(startX.toInt(), startY, startX.toInt(), endY, 10)
        return uiDevice
    }
}
