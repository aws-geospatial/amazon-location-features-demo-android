package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
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
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_ADDRESS
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_MESSAGE_FOUND
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class AnalyticsTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)

        super.before()
    }

    /**
     * This method check successful record of below mentioned events,
     * PLACES_SEARCH, SCREEN_OPEN, MAP_UNIT_CHANGE
     */
    @Test
    fun checkAnalyticsContent() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/edt_search_places")),
                DELAY_5000,
            )
            // Start - Search event check
            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(ViewActions.replaceText(TEST_ADDRESS))
            var snackBarMsg =
                uiDevice.wait(Until.hasObject(By.text(EventType.PLACE_SEARCH)), DELAY_10000)
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg)
            edtSearch.perform(ViewActions.closeSoftKeyboard())
            val clSearchSheet =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_search)
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clSearchSheet)
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.menu_explore))),
                DELAY_5000,
            )
            val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
            onView(
                allOf(
                    withText(settingTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed(),
                ),
            ).perform(click())
            snackBarMsg =
                uiDevice.wait(Until.hasObject(By.text(EventType.SCREEN_OPEN)), DELAY_10000)
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg)
            // End - Screen change event test
            // Start - Map unit change event test
            onView(
                allOf(
                    withId(R.id.cl_unit_system),
                    isDisplayed(),
                ),
            ).perform(click())

            onView(
                allOf(
                    withId(R.id.ll_imperial),
                    isDisplayed(),
                ),
            ).perform(click())

            snackBarMsg =
                uiDevice.wait(Until.hasObject(By.text(EventType.MAP_UNIT_CHANGE)), DELAY_10000)
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg)
            // End - Map unit change event test
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    override fun after() {
        super.after()
        val targetContext = ApplicationProvider.getApplicationContext<Context>()
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
    }
}
