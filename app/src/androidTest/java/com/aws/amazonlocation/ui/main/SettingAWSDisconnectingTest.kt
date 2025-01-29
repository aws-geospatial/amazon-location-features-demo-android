package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_POOL_ID_NOT_BLANK
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingAWSDisconnectingTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())
    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun delay() {
        val activity: MainActivity = mActivityRule.activity
        preferenceManager = (activity as BaseActivity).mPreferenceManager
    }

    @Test
    fun showSettingAWSDisconnectingTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
        onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        ).perform(ViewActions.click())
        onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        onView(withId(R.id.ns_cloud_formation)).perform(swipeUp())

        val region =
            uiDevice.findObject(By.text("Canada (Central) ca-central-1"))
        region?.click()

        val logOut =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.disconnect_aws)))
        logOut?.click()

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.disconnect_aws))),
            DELAY_20000
        )
        val disconnectAws =
            onView(withId(android.R.id.button1)).check(ViewAssertions.matches(isDisplayed()))
        disconnectAws.perform(ViewActions.click())

        val poolId = preferenceManager.getValue(KEY_POOL_ID, "")
        Assert.assertTrue(TEST_FAILED_POOL_ID_NOT_BLANK, poolId == "")
    }
}
