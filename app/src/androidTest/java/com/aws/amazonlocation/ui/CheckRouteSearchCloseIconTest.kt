package com.aws.amazonlocation.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
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
class CheckRouteSearchCloseIconTest : BaseTestMainActivity() {
    @Test
    fun showCheckRouteSearchCloseIconTest() {
        try {
            checkLocationPermission()

            val cardDirection =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirection.perform(click())

            val edtSearchDest =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.edt_search_dest),
                        isDisplayed(),
                    ),
                )
            edtSearchDest?.perform(
                click(),
                replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD),
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.iv_close_destination),
                    isDisplayed(),
                ),
            )?.perform(click())

            edtSearchDest?.perform(
                click(),
                replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD),
            )

            val rvSearchPlaces =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.rv_search_places_suggestion_direction),
                        isDisplayed(),
                        hasMinimumChildCount(1),
                    ),
                )
            rvSearchPlaces?.perform(
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
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
