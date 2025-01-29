package com.aws.amazonlocation.ui.main

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.actions.nestedScrollTo
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.*

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ConnectToAWSTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())
    private lateinit var bottomNavigation: BottomNavigationView

    @Throws(java.lang.Exception::class)
    override fun before() {
        val targetContext: Context = getInstrumentation().targetContext.applicationContext
        val pm = PreferenceManager(targetContext)
        pm.setDefaultConfig()
        pm.setValue(IS_APP_FIRST_TIME_OPENED, true)
        pm.setValue(KEY_RE_START_APP, false)

        super.before()
        val activity: MainActivity = mActivityRule.activity
        bottomNavigation = activity.findViewById(R.id.bottom_navigation_main)
    }

    @Test
    fun canConnectToAWSFromSettings() {
        try {
            checkLocationPermission(uiDevice)
            val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

            onView(
                allOf(
                    withText(settingTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed(),
                ),
            ).perform(click())
            Assert.assertEquals(true, bottomNavigation.menu.findItem(R.id.menu_settings).isChecked)

            onView(
                allOf(
                    withId(R.id.cl_aws_cloudformation),
                    isDisplayed(),
                ),
            ).perform(click())

            onView(withId(R.id.edt_identity_pool_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.IDENTITY_POOL_ID),
                closeSoftKeyboard(),
            )
            onView(withId(R.id.edt_user_domain)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_DOMAIN),
                closeSoftKeyboard(),
            )
            onView(withId(R.id.edt_user_pool_client_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_POOL_CLIENT_ID),
                closeSoftKeyboard(),
            )
            onView(withId(R.id.edt_user_pool_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_POOL_ID),
                closeSoftKeyboard(),
            )
            onView(withId(R.id.edt_web_socket_url)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.WEB_SOCKET_URL),
                closeSoftKeyboard(),
            )
            val btnConnect =
                onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
            btnConnect.perform(click())
            uiDevice.wait(Until.hasObject(By.text(mActivityRule.activity.getString(R.string.sign_in))), DELAY_5000)
            val signIn =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.sign_in)))
            Assert.assertNotNull(signIn)
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED_CONNECT_TO_AWS_FROM_SETTINGS)
        }
    }
}
