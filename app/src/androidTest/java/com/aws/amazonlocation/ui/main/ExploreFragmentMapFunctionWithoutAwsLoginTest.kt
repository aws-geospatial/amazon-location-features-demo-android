package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.actions.swipeLeft
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapFunctionWithoutAwsLoginTest : BaseTestMainActivity() {
    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun setUp() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(
            KEY_MAP_STYLE_NAME,
            mActivityRule.activity.getString(R.string.map_standard)
        )
    }

    @Test
    fun showMapFunctionWithoutAwsLoginTest() {
        checkLocationPermission()
        waitForView(
            allOf(
                withId((R.id.mapView)),
                isDisplayed()
            )
        )
        onView(withId(R.id.mapView)).perform(swipeLeft())
        val btnCardMap =
            onView(withId(R.id.card_map)).check(ViewAssertions.matches(isDisplayed()))
        btnCardMap?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.map_monochrome)),
                isDisplayed()
            )
        )?.perform(click())

        val ivMapStyleClose =
            onView(withId(R.id.iv_map_style_close)).check(ViewAssertions.matches(isDisplayed()))
        ivMapStyleClose?.perform(click())

        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch?.perform(click())
        onView(withId(R.id.edt_search_places))?.perform(replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))
        val rvSearchPlaceSuggestion =
            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
        var itemCount = 0
        rvSearchPlaceSuggestion?.check { view, _ ->
            if (view is RecyclerView) {
                itemCount = view.adapter?.itemCount ?: 0
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        }

        if (itemCount > 0) {
            onView(withId(R.id.rv_search_places_suggestion))?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )
            waitForView(
                allOf(
                    withId(R.id.tv_direction_time),
                    isDisplayed()
                )
            )

            val btnDirection =
                waitForView(
                    allOf(
                        withId(R.id.btn_direction),
                        isDisplayed()
                    )
                )
            btnDirection?.perform(click())

            val cardDriveGo =
                waitForView(
                    allOf(
                        withId(R.id.card_drive_go),
                        isDisplayed()
                    )
                )
            cardDriveGo?.perform(click())

            waitForView(
                allOf(
                    withId(R.id.rv_navigation_list),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
        } else {
            Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
        }
    }
}
