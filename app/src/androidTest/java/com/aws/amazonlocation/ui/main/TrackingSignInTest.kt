package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingSignInTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Before
    fun delay() {
        Thread.sleep(DELAY_10000)
    }

    @Test
    fun showSignInTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_10000)
        val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
        tracking.click()
        Thread.sleep(DELAY_1000)
        val signIn =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.sign_in)))
        signIn?.click()
        Thread.sleep(DELAY_5000)
        onView(withId(R.id.sign_in_web_view))
            .check(matches(isDisplayed()))

        onWebView(withId(R.id.sign_in_web_view))
            .forceJavascriptEnabled()

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "username"))
            .perform(webKeys(BuildConfig.USER_LOGIN_NAME))

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "password"))
            .perform(webKeys(BuildConfig.USER_LOGIN_PASSWORD))

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "signInSubmitButton"))
            .perform(webClick())

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker))),
            DELAY_20000
        )

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker))),
            DELAY_1000
        )
        val labelContinue =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker)))
        labelContinue?.click()

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
            DELAY_1000
        )
    }
}
