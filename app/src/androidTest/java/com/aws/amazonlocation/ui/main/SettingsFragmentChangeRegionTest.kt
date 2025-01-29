package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.ui.main.region.RegionAdapter
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.*

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentChangeRegionTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Throws(java.lang.Exception::class)
    override fun before() {
        super.before()
    }

    @Test
    fun checkChangeRegion() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            goToRegion()
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun goToRegion() {
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
        onView(
            allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        ).perform(click())

        waitForView(CoreMatchers.allOf(withId(R.id.cl_region), isDisplayed()))
        onView(
            allOf(
                withId(R.id.cl_region),
                isDisplayed(),
            ),
        ).perform(click())

        waitForView(CoreMatchers.allOf(withId(R.id.rv_region), isDisplayed()))
        val listRv =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_region),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
        listRv?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2,
                click(),
            ),
        )
        var rbRegionIsChecked = false
        onView(withId(R.id.rv_region)).perform(
            RecyclerViewActions.scrollToPosition<RegionAdapter.RegionVH>(
                2,
            ),
            RecyclerViewActions.actionOnItemAtPosition<RegionAdapter.RegionVH>(
                2,
                object : ViewAction {
                    override fun getConstraints(): Matcher<View> = isAssignableFrom(TextView::class.java)

                    override fun getDescription(): String = "Get data from RecyclerView item"

                    override fun perform(
                        uiController: UiController?,
                        view: View,
                    ) {
                        val rbRegion =
                            view.findViewById<AppCompatRadioButton>(R.id.rb_region)
                        rbRegionIsChecked = rbRegion.isSelected
                    }
                },
            ),
        )
        Assert.assertTrue(TEST_FAILED, rbRegionIsChecked)
    }
}
