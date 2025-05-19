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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.SEARCH_TEST_WORD_1
import com.aws.amazonlocation.SEARCH_TEST_WORD_2
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_AVOID_DIRT_ROADS
import com.aws.amazonlocation.utils.KEY_AVOID_FERRIES
import com.aws.amazonlocation.utils.KEY_AVOID_TOLLS
import com.aws.amazonlocation.utils.KEY_AVOID_TUNNELS
import com.aws.amazonlocation.utils.KEY_AVOID_U_TURNS
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentDefaultRouteTest : BaseTestMainActivity() {
    @Throws(java.lang.Exception::class)
    override fun before() {
        val preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())

        // Default route options disabled by default.
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_AVOID_TOLLS)
        preferenceManager.removeValue(KEY_AVOID_FERRIES)
        preferenceManager.removeValue(KEY_AVOID_DIRT_ROADS)
        preferenceManager.removeValue(KEY_AVOID_U_TURNS)
        preferenceManager.removeValue(KEY_AVOID_TUNNELS)

        super.before()
    }

    @Test
    fun checkDefaultRouteOptionsTest() {
        try {
            checkLocationPermission()

            goToDefaultRouteSettings()

            toggleSwitch(R.id.switch_avoid_tools)
            toggleSwitch(R.id.switch_avoid_ferries)
            toggleSwitch(R.id.switch_avoid_dirt_roads)
            toggleSwitch(R.id.switch_avoid_u_turns)
            toggleSwitch(R.id.switch_avoid_tunnels)

            checkDefaultRouteOptions(
                avoidTollsShouldBe = true,
                avoidFerriesShouldBe = true,
                avoidDirtRoadShouldBe = true,
                avoidUTurnShouldBe = true,
                avoidTunnelsShouldBe = true
            )
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToDefaultRouteSettings() {
        waitForView(
            allOf(
                withId(R.id.bottom_navigation_main),
                isDisplayed()
            )
        )

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(
            allOf(
                withId(R.id.cl_route_option),
                isDisplayed()
            )
        )
            ?.perform(click())
    }

    private fun toggleSwitch(
        @IdRes switchId: Int
    ) {
        waitForView(
            allOf(
                withId(switchId),
                isDisplayed()
            )
        )?.perform(click())
    }

    private fun checkDefaultRouteOptions(
        avoidTollsShouldBe: Boolean,
        avoidFerriesShouldBe: Boolean,
        avoidDirtRoadShouldBe: Boolean,
        avoidUTurnShouldBe: Boolean,
        avoidTunnelsShouldBe: Boolean
    ) {
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_navigate)),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(
            CoreMatchers.allOf(
                withContentDescription(AMAZON_MAP_READY),
                isDisplayed()
            )
        )

        val cardDirectionTest =
            onView(withId(R.id.card_direction)).check(ViewAssertions.matches(isDisplayed()))
        cardDirectionTest.perform(click())

        val sourceEdt =
            waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
        sourceEdt?.perform(replaceText(SEARCH_TEST_WORD_1))

        val suggestionListSrcRv =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
        suggestionListSrcRv?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        val destinationEdt =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed()
                )
            )
        destinationEdt?.perform(click())
        destinationEdt?.perform(typeText(SEARCH_TEST_WORD_2))

        val suggestionListDestRv =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
        suggestionListDestRv?.perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        waitForView(
            CoreMatchers.allOf(
                withId(R.id.card_drive_go),
                isDisplayed()
            )
        )

        val cardRoutingOption =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_routing_option),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )
        cardRoutingOption?.perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.switch_avoid_tools)).check { view, _ ->
            if (view is SwitchCompat) {
                Assert.assertTrue(
                    TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED,
                    view.isChecked == avoidTollsShouldBe
                )
            }
        }

        onView(withId(R.id.switch_avoid_ferries)).check { view, _ ->
            if (view is SwitchCompat) {
                Assert.assertTrue(
                    TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED,
                    view.isChecked == avoidFerriesShouldBe
                )
            }
        }

        onView(withId(R.id.switch_avoid_dirt_roads)).check { view, _ ->
            if (view is SwitchCompat) {
                Assert.assertTrue(
                    TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED,
                    view.isChecked == avoidDirtRoadShouldBe
                )
            }
        }

        onView(withId(R.id.switch_avoid_tunnels)).check { view, _ ->
            if (view is SwitchCompat) {
                Assert.assertTrue(
                    TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED,
                    view.isChecked == avoidTunnelsShouldBe
                )
            }
        }

        onView(withId(R.id.switch_avoid_u_turns)).check { view, _ ->
            if (view is SwitchCompat) {
                Assert.assertTrue(
                    TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED,
                    view.isChecked == avoidUTurnShouldBe
                )
            }
        }
    }
}
