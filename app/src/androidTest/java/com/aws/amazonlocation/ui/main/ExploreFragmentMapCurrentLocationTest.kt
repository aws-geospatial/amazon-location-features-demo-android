package com.aws.amazonlocation.ui.main

import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapCurrentLocationTest : BaseTestMainActivity() {
    @Test
    fun testMapCheckCurrentLocation() {
        try {
            checkLocationPermission()
            var mapbox: MapLibreMap? = null

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
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
