package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
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
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.SECOND_DELAY_60
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NOT_TRACKING_EXIT_DIALOG
import com.aws.amazonlocation.TRACKING_ENTERED
import com.aws.amazonlocation.TRACKING_EXITED
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingGeofenceExitTest : BaseTestMainActivity() {

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
                DELAY_1000
            )
            val labelStartTracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
            labelStartTracking?.click()

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.ok))),
                SECOND_DELAY_60
            )
            var dialogText = getAlertDialogMessage()
            if (dialogText.contains(TRACKING_ENTERED)) {
                val labelOk =
                    uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.ok)))
                labelOk?.click()
                Thread.sleep(DELAY_1000)

                uiDevice.wait(
                    Until.hasObject(By.text(mActivityRule.activity.getString(R.string.ok))),
                    SECOND_DELAY_60
                )
                dialogText = getAlertDialogMessage()
                Assert.assertTrue(TEST_FAILED_NOT_TRACKING_EXIT_DIALOG, dialogText.contains(TRACKING_EXITED))
            } else if (dialogText.contains(TRACKING_EXITED)) {
                Assert.assertTrue(TEST_FAILED_NOT_TRACKING_EXIT_DIALOG, dialogText.contains(TRACKING_EXITED))
            } else {
                failTest(119, null)
                Assert.fail(TEST_FAILED)
            }
        } catch (e: Exception) {
            failTest(123, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun getAlertDialogMessage(): String {
        val appCompatTextViewMatcher = withId(android.R.id.message)
        var messageText = ""
        onView(appCompatTextViewMatcher).perform(object : ViewAction {
            override fun getDescription(): String {
                return "get AlertDialog message"
            }

            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
            }

            override fun perform(uiController: UiController?, view: View?) {
                val textView = view as TextView?
                messageText = textView?.text.toString()
            }
        })
        return messageText
    }
}
