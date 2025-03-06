package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_COUNTRY
import com.aws.amazonlocation.TEST_WORD_ARG
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentPoliticalViewTest : BaseTestMainActivity() {
    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    @Test
    fun testPoliticalViewChange() {
        try {
            checkLocationPermission()

            goToMapStyles()

            val clPoliticalView =
                onView(withId(R.id.cl_political_view)).check(matches(isDisplayed()))
            clPoliticalView.perform(click())

            val etSearchCountry =
                onView(withId(R.id.et_search_country)).check(matches(isDisplayed()))
            etSearchCountry.perform(click())
            onView(withId(R.id.et_search_country)).perform(replaceText(TEST_WORD_ARG))

            waitForView(
                AllOf.allOf(
                    withText(mActivityRule.activity.getString(R.string.description_arg)),
                    isDisplayed()
                )
            )
            val rbCountry =
                onView(withId(R.id.rb_country)).check(matches(isDisplayed()))
            rbCountry.perform(click())

            val tvPoliticalDescription =
                waitForView(
                    allOf(
                        withId(R.id.tv_political_description),
                        isDisplayed()
                    )
                )

            tvPoliticalDescription?.check { view, _ ->
                if (view is AppCompatTextView) {
                    Assert.assertTrue(TEST_FAILED_COUNTRY, view.text.contains(TEST_WORD_ARG))
                }
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun goToMapStyles() {
        val cardMap = waitForView(allOf(withId(R.id.card_map), isDisplayed()))
        cardMap?.perform(click())

        swipeUp()
    }

    private fun swipeUp(): UiDevice? {
        // Get the screen dimensions
        val screenHeight =
            getInstrumentation()
                .targetContext.resources.displayMetrics.heightPixels

        // Set the starting point for the swipe (bottom-center of the screen)
        val startX =
            getInstrumentation()
                .targetContext.resources.displayMetrics.widthPixels / 2f
        val startY = screenHeight - 100 // Offset from the bottom of the screen

        // Set the ending point for the swipe (top-center of the screen)
        val endY = 100 // Offset from the top of the screen

        // Perform the swipe action
        val uiDevice = UiDevice.getInstance(getInstrumentation())
        uiDevice.swipe(startX.toInt(), startY, startX.toInt(), endY, 10)
        return uiDevice
    }
}
