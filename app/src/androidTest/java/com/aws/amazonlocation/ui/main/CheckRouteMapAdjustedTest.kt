package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_CARD
import com.aws.amazonlocation.TEST_FAILED_SEARCH_DIRECTION
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED
import com.aws.amazonlocation.TEST_WORD_AUBURN_SYDNEY
import com.aws.amazonlocation.TEST_WORD_MANLY_BEACH_SYDNEY
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteMapAdjustedTest : BaseTestMainActivity() {


    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showRouteMapAdjustedTest() {
        try {
            var mapbox: MapLibreMap? = null
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)
            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_5000)
            val beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
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
                Thread.sleep(DELAY_3000)
                val edtSearchDirection =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                if (edtSearchDirection.visibility == View.VISIBLE) {
                    onView(withId(R.id.edt_search_direction)).perform(
                        ViewActions.replaceText(
                            TEST_WORD_AUBURN_SYDNEY,
                        ),
                    )
                    Thread.sleep(DELAY_2000)
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                        DELAY_20000,
                    )
                    Thread.sleep(DELAY_3000)
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
                    onView(withId(R.id.edt_search_dest)).perform(ViewActions.typeText(TEST_WORD_MANLY_BEACH_SYDNEY))
                    Thread.sleep(DELAY_3000)
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                        DELAY_20000,
                    )
                    Thread.sleep(DELAY_3000)
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
                    Thread.sleep(DELAY_5000)
                    if (beforeZoomLevel != null) {
                        mapbox?.cameraPosition?.zoom?.let {
                            Assert.assertTrue(TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED, beforeZoomLevel != it)
                        }
                    } else {
                        Assert.fail(TEST_FAILED_ZOOM_LEVEL)
                    }
                } else {
                    Assert.fail(TEST_FAILED_SEARCH_DIRECTION)
                }
            } else {
                Assert.fail(TEST_FAILED_DIRECTION_CARD)
            }
        } catch (e: Exception) {
            failTest(146, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
