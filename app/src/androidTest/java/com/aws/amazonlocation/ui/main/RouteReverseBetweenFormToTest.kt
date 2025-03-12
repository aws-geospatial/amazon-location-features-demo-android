package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class RouteReverseBetweenFormToTest : BaseTestMainActivity() {
    @Test
    fun showRouteReverseBetweenFormToTest() {
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

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO)
                    ),
                    isDisplayed()
                )
            )

            val originText = StringBuilder()
            val destinationText = StringBuilder()
            val swappedOriginText = StringBuilder()
            val swappedDestinationText = StringBuilder()

            onView(withId(R.id.edt_search_direction)).check { view, _ ->
                if (view is TextInputEditText) {
                    originText.append(view.text.toString().trim())
                }
            }

            onView(withId(R.id.edt_search_dest)).check { view, _ ->
                if (view is TextInputEditText) {
                    destinationText.append(view.text.toString().trim())
                }
            }

            val swapBtn =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.iv_swap_location),
                        isDisplayed()
                    )
                )
            swapBtn?.perform(click())

            onView(withId(R.id.edt_search_dest)).check { view, _ ->
                if (view is TextInputEditText) {
                    swappedDestinationText.append(view.text.toString().trim())
                }
            }

            onView(withId(R.id.edt_search_direction)).check { view, _ ->
                if (view is TextInputEditText) {
                    swappedOriginText.append(view.text.toString().trim())
                }
            }
            Assert.assertTrue(
                TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT,
                originText.toString() == swappedDestinationText.toString() && destinationText.toString() == swappedOriginText.toString()
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
