package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_INVALID_IDENTITY_POOL_ID
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.scrollForView
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingAwsConnectTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showAwsConnectTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_3000)

        val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
        tracking.click()

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
            DELAY_1000
        )

        val clEnableTracking =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_enable_tracking)
        if (clEnableTracking.visibility == View.VISIBLE) {
            val btnTryTracker =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_enable_tracking)
            mActivityRule.activity.runOnUiThread {
                btnTryTracker.performClick()
            }
        }
        Thread.sleep(DELAY_1000)
        val appViews = UiScrollable(UiSelector().scrollable(true))

        val edtIdentityPoolId = scrollForView(allOf(withId(R.id.edt_identity_pool_id), isCompletelyDisplayed())){
            appViews.scrollForward(2)
        }
        edtIdentityPoolId?.perform(replaceText(BuildConfig.IDENTITY_POOL_ID))

        val edtUserDomain = scrollForView(allOf(withId(R.id.edt_user_domain), isCompletelyDisplayed())){
            appViews.scrollForward(2)
        }
        edtUserDomain?.perform(replaceText(BuildConfig.USER_DOMAIN))

        val edtUserPoolClientId = scrollForView(allOf(withId(R.id.edt_user_pool_client_id), isCompletelyDisplayed())) {
            appViews.scrollForward(2)
        }
        edtUserPoolClientId?.perform(replaceText(BuildConfig.USER_POOL_CLIENT_ID))

        val edtUserPoolId = scrollForView(allOf(withId(R.id.edt_user_pool_id), isCompletelyDisplayed())) {
            appViews.scrollForward(2)
        }
        edtUserPoolId?.perform(replaceText(BuildConfig.USER_POOL_ID))

        val edtWebSocketUrl = scrollForView(allOf(withId(R.id.edt_web_socket_url), isCompletelyDisplayed())) {
            appViews.scrollForward(2)
        }
        edtWebSocketUrl?.perform(replaceText(BuildConfig.WEB_SOCKET_URL))

        val btnConnect =
            onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
        btnConnect.perform(click())
        Thread.sleep(DELAY_5000)

        val targetContext: Context = getInstrumentation().targetContext.applicationContext
        val pm = PreferenceManager(targetContext)
        val mPoolId = pm.getValue(KEY_POOL_ID, "")

        val packageName = targetContext.packageName
        // Clear app from recent apps list
        val am = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.let {
            it.appTasks.forEach { task ->
                if (task.taskInfo.baseActivity?.packageName == packageName) {
                    task.finishAndRemoveTask()
                }
            }
        }
        Assert.assertTrue(TEST_FAILED_INVALID_IDENTITY_POOL_ID, mPoolId == BuildConfig.IDENTITY_POOL_ID)
    }
}
