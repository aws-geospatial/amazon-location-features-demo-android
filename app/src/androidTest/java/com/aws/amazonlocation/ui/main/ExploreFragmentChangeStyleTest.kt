package com.aws.amazonlocation.ui.main

import androidx.appcompat.app.AppCompatActivity
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
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentChangeStyleTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager
    private val latch = CountDownLatch(1)

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    private fun getActivity(): AppCompatActivity {
        return mActivityRule.activity
    }

    private fun getCurrentMapName(): String {
        val mapStyleNameDisplay =
            preferenceManager.getValue(
                KEY_MAP_STYLE_NAME,
                getActivity().getString(R.string.map_light)
            )
                ?: getActivity().getString(R.string.map_light)
        val mapName: String

        when (mapStyleNameDisplay) {
            getActivity().getString(R.string.map_light) -> {
                mapName = MapNames.ESRI_LIGHT
            }
            getActivity().getString(R.string.map_streets) -> {
                mapName = MapNames.ESRI_STREET_MAP
            }
            getActivity().getString(R.string.map_navigation) -> {
                mapName = MapNames.ESRI_NAVIGATION
            }
            getActivity().getString(R.string.map_dark_gray) -> {
                mapName = MapNames.ESRI_DARK_GRAY_CANVAS
            }
            getActivity().getString(R.string.map_light_gray) -> {
                mapName = MapNames.ESRI_LIGHT_GRAY_CANVAS
            }
            getActivity().getString(R.string.map_imagery) -> {
                mapName = MapNames.ESRI_IMAGERY
            }
            getActivity().getString(R.string.map_contrast) -> {
                mapName = MapNames.HERE_CONTRAST
            }
            getActivity().getString(R.string.map_explore) -> {
                mapName = MapNames.HERE_EXPLORE
            }
            getActivity().getString(R.string.map_explore_truck) -> {
                mapName = MapNames.HERE_EXPLORE_TRUCK
            }
            getActivity().getString(R.string.map_hybrid) -> {
                mapName = MapNames.HERE_HYBRID
            }
            getActivity().getString(R.string.map_raster) -> {
                mapName = MapNames.HERE_IMAGERY
            }
            getActivity().getString(R.string.map_standard_dark) -> {
                mapName = MapNames.OPEN_DATA_STANDARD_DARK
            }
            else -> {
                mapName = MapNames.ESRI_LIGHT
            }
        }

        return mapName
    }

    @Test
    fun testMapStyleChange() {
        try {
            var mapbox: MapLibreMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_2000)

            goToMapStyles()

            // Check esri maps styles
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_ESRI_1), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_ESRI_2), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_ESRI_3), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_ESRI_4), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_ESRI_5), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)

            // Check here maps styles
            waitForView(withId(R.id.rv_map_style))?.perform(
                actionOnItemAtPosition<ViewHolder>(
                    1,
                    click()
                )
            )
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_HERE_1), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_HERE_2), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_HERE_3), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)
            waitForView(allOf(withContentDescription(STYLE_TAG_HERE_4), isDisplayed()))?.perform(click())
            checkLoadedTheme(mapbox)

            swipeUp()
            waitForView(allOf(withContentDescription(STYLE_TAG_OPEN_1), isDisplayed()))?.perform(click())
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_open_data))),
                DELAY_2000
            )

            val labelOk =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_enable_open_data)))
            labelOk?.click()
            checkLoadedTheme(mapbox)
        } catch (e: Exception) {
            failTest(147, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToMapStyles() {
        val cardMap = waitForView(allOf(withId(R.id.card_map), isDisplayed()))
        cardMap?.perform(click())

        Thread.sleep(DELAY_2000)

        getInstrumentation().runOnMainSync {
            val clSearchSheet = mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
            if (clSearchSheet.isVisible) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(clSearchSheet)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                Assert.fail(TEST_FAILED_STYLE_SHEET)
            }
        }
    }

    private fun checkLoadedTheme(mapboxMap: MapLibreMap?) {
        Thread.sleep(DELAY_3000)

        getInstrumentation().waitForIdleSync()
        getInstrumentation().runOnMainSync {
            mapboxMap?.getStyle {
                val correctStyleLoaded = it.json.let { json ->

                    val obj = JSONObject(json).getJSONObject(JSON_KEY_SOURCES)

                    val key = if (obj.has(JSON_KEY_ESRI)) {
                        JSON_KEY_ESRI
                    } else if (obj.has(JSON_KEY_HERE)) {
                        JSON_KEY_HERE
                    } else if (obj.has(JSON_KEY_RASTER_TILES)) {
                        JSON_KEY_RASTER_TILES
                    } else if (obj.has(JSON_KEY_AWS)) {
                        JSON_KEY_AWS
                    } else {
                        null
                    }

                    if (key != null) {
                        val source = obj.getJSONObject(key)
                        val tiles = source.getJSONArray(JSON_KEY_TILES)
                        if (tiles.length() > 0) {
                            tiles.getString(0)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }?.contains(getCurrentMapName()) == true

                if (!correctStyleLoaded) {
                    Assert.fail(TEST_FAILED_INCORRECT_STYLE)
                }

                latch.countDown()
            }
        }

        latch.await()
        Thread.sleep(DELAY_2000)
    }

    private fun swipeUp(): UiDevice? {
        // Get the screen dimensions
        val screenHeight = getInstrumentation().targetContext.resources.displayMetrics.heightPixels

        // Set the starting point for the swipe (bottom-center of the screen)
        val startX = getInstrumentation().targetContext.resources.displayMetrics.widthPixels / 2f
        val startY = screenHeight - 100 // Offset from the bottom of the screen

        // Set the ending point for the swipe (top-center of the screen)
        val endY = 100 // Offset from the top of the screen

        // Perform the swipe action
        val uiDevice = UiDevice.getInstance(getInstrumentation())
        uiDevice.swipe(startX.toInt(), startY, startX.toInt(), endY, 10)
        return uiDevice
    }
}
