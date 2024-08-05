package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
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

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showRouteReverseBetweenFormToTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            Thread.sleep(DELAY_2000)

            val sourceEdt = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click())

            Thread.sleep(DELAY_2000)

            val clMyLocation =
                waitForView(CoreMatchers.allOf(withText(R.string.label_my_location), isDisplayed()))
            clMyLocation?.perform(click())

            Thread.sleep(DELAY_2000)

            val destinationEdt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed(),
                ),
            )
            destinationEdt?.perform(click(), ViewActions.replaceText(TEST_WORD_4))

            Thread.sleep(DELAY_2000)

            val suggestionListRv = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            // btnCarGo
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO),
                    ),
                    isDisplayed(),
                ),
            )

            lateinit var originText: String
            lateinit var destinationText: String

            Thread.sleep(DELAY_3000)

            getInstrumentation().runOnMainSync {
                val edtSearchDirection =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                val edtSearchDest =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_dest)
                originText = edtSearchDirection.text.toString().trim()
                destinationText = edtSearchDest.text.toString().trim()
            }

            val swapBtn = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.iv_swap_location),
                    isDisplayed(),
                ),
            )
            swapBtn?.perform(click())

            Thread.sleep(DELAY_3000)

            getInstrumentation().runOnMainSync {
                val edtSearchDirection =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                val edtSearchDest =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_dest)
                val swappedOriginText = edtSearchDirection.text.toString().trim()
                val swappedDestinationText = edtSearchDest.text.toString().trim()
                Assert.assertTrue(TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT, originText == swappedDestinationText && destinationText == swappedOriginText)
            }
        } catch (e: Exception) {
            failTest(133, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
