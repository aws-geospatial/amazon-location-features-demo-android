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
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_ADDRESS
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_MESSAGE_FOUND
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class AnalyticsTest : BaseTestMainActivity() {
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
            checkLocationPermission()
            // Start - Search event check
            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(ViewActions.replaceText(TEST_ADDRESS))
            waitForView(CoreMatchers.allOf(withText(EventType.PLACE_SEARCH), isDisplayed()))
            var snackBarMsg =
                waitForView(CoreMatchers.allOf(withText(EventType.PLACE_SEARCH), isDisplayed()))
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg != null)
            edtSearch.perform(ViewActions.closeSoftKeyboard())
            val clSearchSheet =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_search)
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clSearchSheet)
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
            waitForView(
                CoreMatchers.allOf(
                    withText(mActivityRule.activity.getString(R.string.menu_navigate)),
                    isDisplayed()
                )
            )
            val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
            onView(
                allOf(
                    withText(settingTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed()
                )
            ).perform(click())
            snackBarMsg =
                waitForView(CoreMatchers.allOf(withText(EventType.SCREEN_OPEN), isDisplayed()))
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg != null)
            // End - Screen change event test
            // Start - Map unit change event test
            onView(
                allOf(
                    withId(R.id.cl_unit_system),
                    isDisplayed()
                )
            ).perform(click())

            onView(
                allOf(
                    withId(R.id.ll_imperial),
                    isDisplayed()
                )
            ).perform(click())

            snackBarMsg =
                waitForView(CoreMatchers.allOf(withText(EventType.MAP_UNIT_CHANGE), isDisplayed()))
            Assert.assertTrue(TEST_FAILED_NO_MESSAGE_FOUND, snackBarMsg != null)
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
