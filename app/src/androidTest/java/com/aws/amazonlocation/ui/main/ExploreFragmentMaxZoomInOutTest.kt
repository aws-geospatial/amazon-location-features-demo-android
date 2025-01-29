package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_MAX_ZOOM_NOT_REACHED
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMaxZoomInOutTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testMapZoomOut() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            var mapbox: MapLibreMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            var beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            var isMaxZoomInReach = false
            while (!isMaxZoomInReach) {
                val map = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView"))
                if (map.exists()) {
                    map.pinchIn(50, 15)
                }
                if (beforeZoomLevel != null) {
                    mapbox?.cameraPosition?.zoom?.let {
                        if (beforeZoomLevel == it) {
                            isMaxZoomInReach = true
                        } else {
                            isMaxZoomInReach = false
                            beforeZoomLevel = mapbox?.cameraPosition?.zoom
                        }
                    }
                }
            }
            Assert.assertTrue(TEST_FAILED_MAX_ZOOM_NOT_REACHED, isMaxZoomInReach)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    @Test
    fun testMaxZoomDoubleTap() {
        try {
            var mapbox: MapLibreMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            var beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            var isMaxZoomInReach = false
            while (!isMaxZoomInReach) {
                val map = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView"))
                if (map.exists()) {
                    onView(withId(R.id.mapView)).perform(ViewActions.doubleClick())
                }
                if (beforeZoomLevel != null) {
                    mapbox?.cameraPosition?.zoom?.let {
                        if (beforeZoomLevel == it) {
                            isMaxZoomInReach = true
                        } else {
                            isMaxZoomInReach = false
                            beforeZoomLevel = mapbox?.cameraPosition?.zoom
                        }
                    }
                }
            }
            Assert.assertTrue(TEST_FAILED_MAX_ZOOM_NOT_REACHED, isMaxZoomInReach)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
