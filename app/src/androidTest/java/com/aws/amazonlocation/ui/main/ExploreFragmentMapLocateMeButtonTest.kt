package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.actions.swipeLeft
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
class ExploreFragmentMapLocateMeButtonTest : BaseTestMainActivity() {
    @Test
    fun testMapCheckLocateMeTest() {
        try {
            var mapbox: MapLibreMap? = null
            checkLocationPermission()

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Espresso.onView(withId(R.id.mapView)).perform(swipeLeft())

            waitForView(
                allOf(
                    withId(R.id.card_navigation),
                    isDisplayed()
                )
            )?.perform(click())
            Assert.assertTrue(
                TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED,
                mapbox?.locationComponent?.isLocationComponentActivated == true &&
                    mapbox?.locationComponent?.isLocationComponentEnabled == true
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
