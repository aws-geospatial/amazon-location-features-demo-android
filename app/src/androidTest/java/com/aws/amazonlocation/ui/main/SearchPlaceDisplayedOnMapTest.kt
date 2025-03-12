package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_RIO_TINTO
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchPlaceDisplayedOnMapTest : BaseTestMainActivity() {
    @Test
    fun showSearchPlaceDisplayedOnMapTest() {
        var mapbox: MapLibreMap? = null
        checkLocationPermission()

        val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_WORD_RIO_TINTO))
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
        if (itemCount >= 0) {
            var sources = 0
            getInstrumentation().runOnMainSync {
                mapbox?.getStyle { style ->
                    sources = style.sources.size
                }
            }
            Assert.assertTrue(TEST_FAILED_IMAGE_NULL, sources > itemCount)
        } else {
            Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
        }
    }
}
