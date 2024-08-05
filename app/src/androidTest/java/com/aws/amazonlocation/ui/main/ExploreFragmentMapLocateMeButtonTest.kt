package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.actions.swipeLeft
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapLocateMeButtonTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testMapCheckLocateMeTest() {
        try {
            var mapbox: MapboxMap? = null
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Espresso.onView(withId(R.id.mapView)).perform(swipeLeft())
            Thread.sleep(DELAY_1000)

            val cardNavigation = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/card_navigation"))
            cardNavigation.click()
            Thread.sleep(DELAY_2000)
            Assert.assertTrue(TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED, mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true)
        } catch (e: Exception) {
            failTest(71, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
