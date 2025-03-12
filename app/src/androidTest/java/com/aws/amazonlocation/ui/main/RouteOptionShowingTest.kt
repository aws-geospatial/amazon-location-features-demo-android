package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class RouteOptionShowingTest : BaseTestMainActivity() {
    @Test
    fun showRouteOptionShowingTest() {
        try {
            checkLocationPermission()

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            val sourceEdt =
                waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click())

            val clMyLocation =
                waitForView(CoreMatchers.allOf(withText(R.string.label_my_location), isDisplayed()))
            clMyLocation?.perform(click())

            val destinationEdt =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.edt_search_dest),
                        isDisplayed()
                    )
                )
            destinationEdt?.perform(click(), ViewActions.replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))

            val suggestionListRv =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.rv_search_places_suggestion_direction),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )
            suggestionListRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            // btnCarGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO)
                    ),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            // btnWalkGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_walk_go),
                    hasDescendant(
                        withText(GO)
                    ),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            // btnTruckGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_truck_go),
                    hasDescendant(
                        withText(GO)
                    ),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            onView(withId(R.id.cl_drive)).check(matches(isDisplayed()))
            onView(withId(R.id.cl_walk)).check(matches(isDisplayed()))
            onView(withId(R.id.cl_truck)).check(matches(isDisplayed()))
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
