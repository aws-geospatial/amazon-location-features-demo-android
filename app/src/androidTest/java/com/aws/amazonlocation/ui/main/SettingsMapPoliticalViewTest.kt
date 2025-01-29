package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsMapPoliticalViewTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

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
    fun testSettingsMapPoliticalViewTest() {
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

        goToMapStyles()

        val clPoliticalView =
            onView(withId(R.id.cl_political_view)).check(matches(isDisplayed()))
        clPoliticalView.perform(click())

        val etSearchCountry =
            onView(withId(R.id.et_search_country)).check(matches(isDisplayed()))
        etSearchCountry.perform(click())
        onView(withId(R.id.et_search_country)).perform(replaceText(TEST_WORD_RUS))

        uiDevice.wait(Until.hasObject(By.text(mActivityRule.activity.getString(R.string.description_rus))), DELAY_5000)

        val rbCountry =
            onView(withId(R.id.rb_country)).check(matches(isDisplayed()))
        rbCountry.perform(click())

        val tvPoliticalDescription = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/tv_political_description"))
        Assert.assertTrue(TEST_FAILED_COUNTRY, tvPoliticalDescription.text.contains(TEST_WORD_RUS))
    }

    private fun goToMapStyles() {
        waitForView(
            AllOf.allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            ?.perform(click())

        waitForView(
            AllOf.allOf(
                withId(R.id.cl_map_style),
                isDisplayed()
            )
        )
            ?.perform(click())
    }
}