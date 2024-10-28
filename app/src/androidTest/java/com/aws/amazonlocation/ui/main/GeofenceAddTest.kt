package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.getRandomGeofenceName
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceAddTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private var geofenceName = getRandomGeofenceName()

    @Test
    fun addGeoFenceTest() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            onView(
                allOf(
                    withId(R.id.card_geofence_map),
                    isDisplayed(),
                ),
            ).perform(click())

            Thread.sleep(DELAY_1000)

            uiDevice.wait(
                Until.gone(By.res("${BuildConfig.APPLICATION_ID}:id/cl_search_loader_geofence_list")),
                DELAY_15000,
            )
            Thread.sleep(DELAY_2000)
            val emptyContainer =
                mActivityRule.activity.findViewById<View>(R.id.cl_empty_geofence_list)

            if (emptyContainer.isVisible) {
                onView(withId(R.id.btn_add_geofence)).perform(click())
            } else {
                onView(withId(R.id.card_add_geofence)).perform(click())
            }
            Thread.sleep(DELAY_5000)
            onView(withId(R.id.edt_add_geofence_search)).perform(clearText(), typeText(TEST_WORD_SHYAMAL_CROSS_ROAD))

            Thread.sleep(DELAY_15000)
            val rvGeofenceSearchPlaces =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_geofence_search_places_suggestion)
            rvGeofenceSearchPlaces.adapter?.itemCount?.let {
                if (it > 0) {
                    onView(withId(R.id.rv_geofence_search_places_suggestion)).perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0,
                            click(),
                        ),
                    )
                }
            }
            Thread.sleep(DELAY_3000)
            val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)
            seekbar.progress = 400

            Thread.sleep(DELAY_1000)

            onView(withId(R.id.edt_enter_geofence_name)).perform(typeText(geofenceName))

            val btnAddGeofenceSave =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_add_geofence_save)
            mActivityRule.activity.runOnUiThread {
                btnAddGeofenceSave.performClick()
            }

            val snackBarMsg = uiDevice.wait(Until.hasObject(By.text("Geofence name already exists")), DELAY_10000)
            if (snackBarMsg == null) {
                uiDevice.wait(
                    Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_geofence")),
                    DELAY_15000
                )

                onView(withId(R.id.rv_geofence)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        hasDescendant(withText(geofenceName)),
                    ),
                )
            } else {
                Assert.assertNotNull(snackBarMsg)
            }
        } catch (e: Exception) {
            failTest(128, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
