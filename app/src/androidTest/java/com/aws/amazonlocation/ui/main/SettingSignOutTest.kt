package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_SIGNIN_BUTTON_NOT_VISIBLE
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingSignOutTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSettingSignInTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

        Espresso.onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        ).perform(ViewActions.click())

        Espresso.onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed(),
            ),
        ).perform(ViewActions.click())
        val isTablet = mActivityRule.activity.resources.getBoolean(R.bool.is_tablet)

        if (!isTablet) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.scrollForward()
        }

        val region =
            uiDevice.findObject(By.text("Canada (Central) ca-central-1"))
        region?.click()

        val logOut =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.log_out)))
        logOut?.click()

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.logout))),
            DELAY_20000,
        )
        val btnLogOut =
            Espresso.onView(withId(android.R.id.button1)).check(ViewAssertions.matches(isDisplayed()))
        btnLogOut.perform(ViewActions.click())

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.sign_in))),
            DELAY_20000,
        )
        val btnSignIn =
            mActivityRule.activity.findViewById<AppCompatButton>(R.id.btn_sign_in)
        Assert.assertTrue(TEST_FAILED_SIGNIN_BUTTON_NOT_VISIBLE, btnSignIn.visibility == View.VISIBLE)
    }
}
