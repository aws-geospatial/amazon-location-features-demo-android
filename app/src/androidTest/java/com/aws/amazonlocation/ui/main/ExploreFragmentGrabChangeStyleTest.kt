package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentGrabChangeStyleTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    @Test
    fun testGrabMapStyleChange() {
        try {
            var mapbox: MapLibreMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_2000)

            goToMapStyles()
            Thread.sleep(DELAY_2000)
            val clMapStyleSheet =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
            if (clMapStyleSheet.visibility == View.VISIBLE) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(clMapStyleSheet)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED

                Thread.sleep(DELAY_2000)
                // Check grab maps styles
                waitForView(withId(R.id.rv_map_style))?.perform(
                    actionOnItemAtPosition<ViewHolder>(
                        2,
                        click()
                    )
                )
                uiDevice.wait(
                    Until.hasObject(By.text(mActivityRule.activity.getString(R.string.ok))),
                    DELAY_2000
                )

                val labelOk =
                    uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.ok)))
                labelOk?.click()
                val mapName =
                    preferenceManager.getValue(KEY_MAP_NAME, mActivityRule.activity.getString(R.string.grab))
                Thread.sleep(DELAY_2000)
                Assert.assertTrue(TEST_FAILED_MAP_STYLE_NOT_CHANGED, mapName == MAP_STYLE_GRAB)
            }
        } catch (e: Exception) {
            failTest(201, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToMapStyles() {
        val cardMap = waitForView(allOf(withId(R.id.card_map), isDisplayed()))
        cardMap?.perform(click())

        Thread.sleep(DELAY_2000)

        getInstrumentation().runOnMainSync {
            val clSearchSheet =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
            if (clSearchSheet.isVisible) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(clSearchSheet)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                Assert.fail(TEST_FAILED_STYLE_SHEET)
            }
        }
    }
}
