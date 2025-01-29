package com.aws.amazonlocation.ui.main

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentEnableLocation : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testMapEnableLocation() {
        try {
            checkLocationPermission(uiDevice)
            try {
                var mapbox: MapLibreMap? = null
                val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
                mapView.getMapAsync {
                    mapbox = it
                }
                waitUntil(DELAY_5000, 25) {
                    mapbox?.locationComponent?.isLocationComponentActivated == true &&
                        mapbox?.locationComponent?.isLocationComponentEnabled == true
                }
                Assert.assertTrue(
                    TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED,
                    mapbox?.locationComponent?.isLocationComponentActivated == true &&
                        mapbox?.locationComponent?.isLocationComponentEnabled == true,
                )
            } catch (e: UiObjectNotFoundException) {
                Assert.fail("$TEST_FAILED ${e.message}")
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
