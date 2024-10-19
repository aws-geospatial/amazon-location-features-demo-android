package com.aws.amazonlocation.ui.main

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_IMAGE_LABEL
import com.aws.amazonlocation.TEST_IMAGE_LABEL_1
import com.aws.amazonlocation.TEST_WORD_1
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchPlaceDisplayedOnMapTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSearchPlaceDisplayedOnMapTest() {
        var mapbox: MapLibreMap? = null
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_1000)

        val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_WORD_1))
        Thread.sleep(DELAY_10000)
        val rvSearchPlaceSuggestion =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
        if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
            rvSearchPlaceSuggestion.adapter?.itemCount?.let {
                if (it >= 0) {
                    mapbox?.getStyle { style ->
                        waitUntil(DELAY_5000, 25) {
                            var image: Bitmap? = null
                            var image1: Bitmap? = null
                            getInstrumentation().runOnMainSync {
                                image = style.getImage(TEST_IMAGE_LABEL)
                                image1 = style.getImage(TEST_IMAGE_LABEL_1)
                            }
                            image != null && image1 != null
                        }
                        getInstrumentation().runOnMainSync {
                            val image = style.getImage(TEST_IMAGE_LABEL)
                            val image1 = style.getImage(TEST_IMAGE_LABEL_1)
                            Assert.assertTrue(TEST_FAILED_IMAGE_NULL, image != null && image1 != null)
                        }
                    }
                } else {
                    Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                }
            }
        } else {
            Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
        }
    }
}
