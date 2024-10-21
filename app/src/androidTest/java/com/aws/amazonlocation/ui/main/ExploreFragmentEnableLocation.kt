package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_4000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_1
import com.aws.amazonlocation.WHILE_USING_THE_APP_2
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
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
            Thread.sleep(DELAY_2000)
            val btnContinueToApp = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
                Thread.sleep(DELAY_2000)
                try {
                    Thread.sleep(DELAY_2000)
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_1))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_2))?.click()
                    uiDevice.findObject(By.text(ALLOW))?.click()
                    Thread.sleep(DELAY_2000)
                    enableGPS(ApplicationProvider.getApplicationContext())
                    uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
                    var mapbox: MapLibreMap? = null
                    val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
                    mapView.getMapAsync {
                        mapbox = it
                    }
                    Thread.sleep(DELAY_4000)
                    waitUntil(DELAY_5000, 25) {
                        mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true
                    }
                    Assert.assertTrue(TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED, mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true)
                } catch (e: UiObjectNotFoundException) {
                    failTest(67, e)
                    Assert.fail(TEST_FAILED)
                }
            } else {
                try {
                    Thread.sleep(DELAY_2000)
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_1))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_2))?.click()
                    uiDevice.findObject(By.text(ALLOW))?.click()
                    Thread.sleep(DELAY_2000)
                    enableGPS(ApplicationProvider.getApplicationContext())
                    uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
                    var mapbox: MapLibreMap? = null
                    val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
                    mapView.getMapAsync {
                        mapbox = it
                    }
                    Thread.sleep(DELAY_4000)
                    waitUntil(DELAY_5000, 25) {
                        mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true
                    }
                    Assert.assertTrue(TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED, mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true)
                } catch (e: UiObjectNotFoundException) {
                    failTest(85, e)
                    Assert.fail(TEST_FAILED)
                }
            }
        } catch (e: Exception) {
            failTest(90, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
