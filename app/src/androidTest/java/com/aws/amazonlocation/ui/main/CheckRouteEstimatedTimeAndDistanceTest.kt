package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_CLOVERDALE_PERTH
import com.aws.amazonlocation.TEST_WORD_KEWDALE_PERTH
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteEstimatedTimeAndDistanceTest : BaseTestMainActivity() {
    @Test
    fun showRouteEstimatedTimeAndDistanceTest() {
        try {
            checkLocationPermission()

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            val sourceEdt =
                waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click(), replaceText(TEST_WORD_CLOVERDALE_PERTH))

            val suggestionListRvSrc =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.rv_search_places_suggestion_direction),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )
            suggestionListRvSrc?.perform(
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
            destinationEdt?.perform(click(), replaceText(TEST_WORD_KEWDALE_PERTH))

            val suggestionListRvDest =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.rv_search_places_suggestion_direction),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )
            suggestionListRvDest?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            // btnCarGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            // btnWalkGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_walk_go),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            // btnTruckGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_truck_go),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )

            val tvDriveDistance = onView(withId(R.id.tv_drive_distance))
            val tvDriveMinute = onView(withId(R.id.tv_drive_minute))
            val tvWalkDistance = onView(withId(R.id.tv_walk_distance))
            val tvWalkMinute = onView(withId(R.id.tv_walk_minute))
            val tvTruckDistance = onView(withId(R.id.tv_truck_distance))
            val tvTruckMinute = onView(withId(R.id.tv_truck_minute))

            tvDriveDistance.check(matches(not(withText(""))))
            tvDriveMinute.check(matches(not(withText(""))))
            tvWalkDistance.check(matches(not(withText(""))))
            tvWalkMinute.check(matches(not(withText(""))))
            tvTruckDistance.check(matches(not(withText(""))))
            tvTruckMinute.check(matches(not(withText(""))))
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
