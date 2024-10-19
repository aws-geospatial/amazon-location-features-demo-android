package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_MAP_NOT_FOUND
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED
import com.aws.amazonlocation.actions.doubleTap
import com.aws.amazonlocation.actions.pinchIn
import com.aws.amazonlocation.actions.pinchOut
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.utils.retry_rule.Retry
import com.aws.amazonlocation.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapZoomInOutTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    @Retry
    fun testMapZoomIn() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            var mapbox: MapLibreMap? = null
            uiDevice.wait(hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_5000)
            val beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            uiDevice.wait(hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/mapView")), DELAY_10000)
            onView(withId(R.id.mapView)).perform(pinchOut(), pinchOut(), pinchOut(), pinchOut(), pinchOut(), pinchOut())
            Thread.sleep(DELAY_5000)
            if (beforeZoomLevel != null) {
                waitUntil(DELAY_3000, 25) {
                    mapbox?.cameraPosition?.zoom?.let {
                        beforeZoomLevel < it
                    }
                }
            }
            if (beforeZoomLevel != null) {
                mapbox?.cameraPosition?.zoom?.let {
                    Assert.assertTrue(TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED, beforeZoomLevel < it)
                }
            } else {
                Assert.fail(TEST_FAILED_ZOOM_LEVEL)
            }
        } catch (e: Exception) {
            failTest(86, e)
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    @Retry
    fun testMapZoomOut() {
        try {
            var mapbox: MapLibreMap? = null
            uiDevice.wait(hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_5000)
            val beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            uiDevice.wait(hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/mapView")), DELAY_10000)
            onView(withId(R.id.mapView)).perform(pinchIn(), pinchIn(), pinchIn(), pinchIn(), pinchIn(), pinchIn())
            Thread.sleep(DELAY_5000)
            if (beforeZoomLevel != null) {
                waitUntil(DELAY_3000, 25) {
                    mapbox?.cameraPosition?.zoom?.let {
                        beforeZoomLevel > it
                    }
                }
            }
            if (beforeZoomLevel != null) {
                mapbox?.cameraPosition?.zoom?.let {
                    Assert.assertTrue(TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED, beforeZoomLevel > it)
                }
            } else {
                Assert.fail(TEST_FAILED_ZOOM_LEVEL)
            }
        } catch (e: Exception) {
            failTest(115, e)
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    @Retry
    fun testMapZoomDoubleTap() {
        try {
            var mapbox: MapLibreMap? = null
            uiDevice.wait(hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_5000)
            val beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            uiDevice.wait(hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/mapView")), DELAY_10000)
            val map = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView"))
            if (map.exists()) {
                onView(withId(R.id.mapView)).perform(doubleTap(), doubleTap(), doubleTap(), doubleTap())
            } else {
                Assert.fail(TEST_FAILED_MAP_NOT_FOUND)
            }
            Thread.sleep(DELAY_5000)
            if (beforeZoomLevel != null) {
                waitUntil(DELAY_3000, 25) {
                    mapbox?.cameraPosition?.zoom?.let {
                        beforeZoomLevel < it
                    }
                }
            }
            if (beforeZoomLevel != null) {
                mapbox?.cameraPosition?.zoom?.let {
                    Assert.assertTrue(TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED, beforeZoomLevel < it)
                }
            } else {
                Assert.fail(TEST_FAILED_ZOOM_LEVEL)
            }
        } catch (e: Exception) {
            failTest(149, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
