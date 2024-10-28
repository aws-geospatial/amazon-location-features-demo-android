package com.aws.amazonlocation.ui.main

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentContentTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var bottomNavigation: BottomNavigationView

    @Throws(java.lang.Exception::class)
    override fun before() {
        super.before()
        val activity: MainActivity = mActivityRule.activity
        bottomNavigation = activity.findViewById(R.id.bottom_navigation_main)
    }

    @Test
    fun checkContent() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val settingsTabText = mActivityRule.activity.getString(R.string.menu_setting)

            onView(
                allOf(
                    withText(settingsTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed()
                )
            )
                .perform(click())

            Thread.sleep(DELAY_1000)

            if (!bottomNavigation.menu.findItem(R.id.menu_settings).isChecked) {
                Assert.fail(TEST_FAILED_NAVIGATION_TAB_SETTINGS_NOT_SELECTED)
            }

            val mapStyle = mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_map_style)
            val defaultRoute = mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_route_option)
            val connectToAws = mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_aws_cloudformation)

            if (!mapStyle.isVisible || !defaultRoute.isVisible || !connectToAws.isVisible) {
                Assert.fail(TEST_FAILED_SETTINGS_ALL_OPTIONS_NOT_VISIBLE)
            }
        } catch (e: Exception) {
            failTest(85, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
