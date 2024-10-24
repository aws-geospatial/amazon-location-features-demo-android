package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY_NULL
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingStartTrackingTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showStartTrackingTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()
            Thread.sleep(DELAY_5000)

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking))),
                DELAY_20000
            )
            val labelStartTracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
            labelStartTracking?.click()

            Thread.sleep(DELAY_20000)

            val rvTracking =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_tracking)

            Thread.sleep(3000)

            if (rvTracking.adapter?.itemCount != null) {
                rvTracking.adapter?.itemCount?.let {
                    Assert.assertTrue(TEST_FAILED_NO_TRACKING_HISTORY, it > 0)
                }
            } else {
                Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY_NULL)
            }
        } catch (e: Exception) {
            failTest(93, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
