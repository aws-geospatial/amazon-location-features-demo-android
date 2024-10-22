package com.aws.amazonlocation.ui.main

import androidx.annotation.IdRes
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.*

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentDefaultRouteTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Throws(java.lang.Exception::class)
    override fun before() {
        val preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())

        // Default route options disabled by default.
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_AVOID_TOLLS)
        preferenceManager.removeValue(KEY_AVOID_FERRIES)

        super.before()
    }

    @Test
    fun checkDefaultRouteOptionsTest() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToDefaultRouteSettings()

            toggleSwitch(R.id.switch_avoid_tools)
            toggleSwitch(R.id.switch_avoid_ferries)

            checkDefaultRouteOptions(avoidTollsShouldBe = true, avoidFerriesShouldBe = true)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToDefaultRouteSettings() {
        waitForView(
            allOf(
                withId(R.id.bottom_navigation_main),
                isDisplayed(),
            ),
        )

        val explorer =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_setting)))
        explorer.click()

        val routeOptions = waitForView(
            allOf(
                withId(R.id.cl_route_option),
                isDisplayed(),
            ),
        )
        routeOptions?.perform(click())
    }

    private fun toggleSwitch(@IdRes switchId: Int) {
        waitForView(
            allOf(
                withId(switchId),
                isDisplayed(),
            ),
        )?.perform(click())
    }

    private fun checkDefaultRouteOptions(avoidTollsShouldBe: Boolean, avoidFerriesShouldBe: Boolean) {
        val explorer =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_explore)))
        explorer.click()

        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        val cardDirectionTest =
            onView(withId(R.id.card_direction)).check(ViewAssertions.matches(isDisplayed()))
        cardDirectionTest.perform(click())

        Thread.sleep(DELAY_2000)

        val sourceEdt = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
        sourceEdt?.perform(replaceText(SEARCH_TEST_WORD_1))

        Thread.sleep(DELAY_2000)

        val suggestionListSrcRv = waitForView(
            CoreMatchers.allOf(
                withId(R.id.rv_search_places_suggestion_direction),
                isDisplayed(),
                hasMinimumChildCount(1),
            ),
        )
        suggestionListSrcRv?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click(),
            ),
        )

        Thread.sleep(DELAY_2000)

        val destinationEdt = waitForView(
            CoreMatchers.allOf(
                withId(R.id.edt_search_dest),
                isDisplayed(),
            ),
        )
        destinationEdt?.perform(click())
        destinationEdt?.perform(typeText(SEARCH_TEST_WORD_2))

        Thread.sleep(DELAY_2000)

        val suggestionListDestRv = waitForView(
            CoreMatchers.allOf(
                withId(R.id.rv_search_places_suggestion_direction),
                isDisplayed(),
                hasMinimumChildCount(1),
            ),
        )
        suggestionListDestRv?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click(),
            ),
        )

        waitForView(
            CoreMatchers.allOf(
                withId(R.id.card_drive_go),
                isDisplayed(),
            ),
        )

        val cardRoutingOption = waitForView(
            CoreMatchers.allOf(
                withId(R.id.card_routing_option),
                withEffectiveVisibility(Visibility.VISIBLE),
            ),
        )
        cardRoutingOption?.perform(click())

        Thread.sleep(DELAY_2000)

        getInstrumentation().waitForIdleSync()
        getInstrumentation().runOnMainSync {
            val switchAvoidToll = mActivityRule.activity.findViewById<SwitchCompat>(R.id.switch_avoid_tools)
            val switchAvoidFerry = mActivityRule.activity.findViewById<SwitchCompat>(R.id.switch_avoid_ferries)

            if (switchAvoidToll.isChecked != avoidTollsShouldBe || switchAvoidFerry.isChecked != avoidFerriesShouldBe) {
                Assert.fail(TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED)
            }
        }
    }
}
