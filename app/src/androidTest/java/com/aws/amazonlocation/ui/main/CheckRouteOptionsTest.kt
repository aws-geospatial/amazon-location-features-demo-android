package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_CARD_DRIVE_GO
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_CARD
import com.aws.amazonlocation.TEST_FAILED_SEARCH_DIRECTION
import com.aws.amazonlocation.TEST_WORD_5
import com.aws.amazonlocation.TEST_WORD_6
import com.aws.amazonlocation.TEST_WORD_7
import com.aws.amazonlocation.TEST_WORD_8
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteOptionsTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showCheckRouteOptionsTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)
            val cardDirection =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_direction)
            if (cardDirection.visibility == View.VISIBLE) {
                val cardDirectionTest =
                    onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
                cardDirectionTest.perform(click())
                uiDevice.wait(
                    Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/edt_search_direction")),
                    DELAY_5000,
                )
                val edtSearchDirection =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                if (edtSearchDirection.visibility == View.VISIBLE) {
                    onView(withId(R.id.edt_search_direction)).perform(ViewActions.typeText(TEST_WORD_5))
                    Thread.sleep(DELAY_2000)
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                        DELAY_20000,
                    )
                    val rvSearchPlacesSuggestionDirection =
                        mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion_direction)
                    rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
                        if (it > 0) {
                            onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    0,
                                    click(),
                                ),
                            )
                        }
                    }
                    onView(withId(R.id.edt_search_dest)).perform(ViewActions.typeText(TEST_WORD_6))
                    Thread.sleep(DELAY_2000)
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                        DELAY_20000,
                    )
                    rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
                        if (it > 0) {
                            onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    0,
                                    click(),
                                ),
                            )
                        }
                    }
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_drive_go")),
                        DELAY_20000,
                    )
                    val cardDriveGo =
                        mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_drive_go)
                    if (cardDriveGo.visibility == View.VISIBLE) {
                        val cardRoutingOption =
                            onView(withId(R.id.card_routing_option)).check(matches(isDisplayed()))
                        cardRoutingOption.perform(click())

                        val switchAvoidTools =
                            onView(withId(R.id.switch_avoid_tools)).check(matches(isDisplayed()))
                        switchAvoidTools.perform(click())
                        Thread.sleep(DELAY_2000)

                        uiDevice.wait(
                            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_drive_go")),
                            DELAY_20000,
                        )
                        if (cardDriveGo.visibility == View.VISIBLE) {
                            onView(withId(R.id.edt_search_direction)).perform(ViewActions.clearText())
                            Thread.sleep(DELAY_2000)
                            onView(withId(R.id.edt_search_direction)).perform(ViewActions.typeText(TEST_WORD_7))
                            Thread.sleep(DELAY_2000)
                            uiDevice.wait(
                                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                                DELAY_20000,
                            )
                            rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
                                if (it > 0) {
                                    onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                            0,
                                            click(),
                                        ),
                                    )
                                }
                            }
                            Thread.sleep(DELAY_2000)
                            onView(withId(R.id.edt_search_dest)).perform(ViewActions.clearText())
                            Thread.sleep(DELAY_2000)
                            onView(withId(R.id.edt_search_dest)).perform(ViewActions.typeText(TEST_WORD_8))
                            Thread.sleep(DELAY_2000)
                            uiDevice.wait(
                                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                                DELAY_20000,
                            )
                            rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
                                if (it > 0) {
                                    onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                            0,
                                            click(),
                                        ),
                                    )
                                }
                            }
                            uiDevice.wait(
                                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_drive_go")),
                                DELAY_20000,
                            )
                            if (cardDriveGo.visibility == View.VISIBLE) {
                                Thread.sleep(DELAY_1000)
                                val switchAvoidFerries =
                                    onView(withId(R.id.switch_avoid_ferries)).check(matches(isDisplayed()))
                                switchAvoidFerries.perform(click())
                                Thread.sleep(DELAY_2000)
                                uiDevice.wait(
                                    Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_drive_go")),
                                    DELAY_20000,
                                )
                                Assert.assertTrue(TEST_FAILED_CARD_DRIVE_GO, cardDriveGo.visibility == View.VISIBLE)
                            } else {
                                Assert.fail(TEST_FAILED_CARD_DRIVE_GO)
                            }
                        } else {
                            Assert.fail(TEST_FAILED_CARD_DRIVE_GO)
                        }
                    } else {
                        Assert.fail(TEST_FAILED_CARD_DRIVE_GO)
                    }
                } else {
                    Assert.fail(TEST_FAILED_SEARCH_DIRECTION)
                }
            } else {
                Assert.fail(TEST_FAILED_DIRECTION_CARD)
            }
        } catch (e: Exception) {
            failTest(208, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
