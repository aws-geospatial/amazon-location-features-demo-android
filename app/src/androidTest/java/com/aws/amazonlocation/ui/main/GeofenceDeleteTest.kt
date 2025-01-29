package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.actions.clickOnViewChild
import com.aws.amazonlocation.actions.clickXYPercent
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.getRandom0_01To1_0
import com.aws.amazonlocation.getRandom1To100
import com.aws.amazonlocation.getRandomGeofenceName
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceDeleteTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var geofenceName: String

    private var initialCount: Int? = null
    private var updatedCount: Int? = null

    @Test
    fun deleteGeoFenceTest() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            onView(
                allOf(
                    withId(R.id.card_geofence_map),
                    isDisplayed(),
                ),
            ).perform(click())

            uiDevice.wait(
                Until.gone(By.res("${BuildConfig.APPLICATION_ID}:id/cl_search_loader_geofence_list")),
                DELAY_5000,
            )

            createOrGetGeoFence()

            deleteGeoFence()

            if (initialCount == null || updatedCount == null || initialCount == updatedCount) {
                Assert.fail(TEST_FAILED)
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun createOrGetGeoFence() {
        val rv = mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_geofence)

        val emptyContainer = mActivityRule.activity.findViewById<View>(R.id.cl_empty_geofence_list)
        if (emptyContainer.isVisible) {
            geofenceName = getRandomGeofenceName()

            onView(
                allOf(
                    withId(R.id.btn_add_geofence),
                    isDisplayed(),
                    isEnabled(),
                ),
            ).perform(click())

            waitForView(CoreMatchers.allOf(withId(R.id.mapView), isDisplayed()))
            onView(
                withId(R.id.mapView),
            ).perform(
                clickXYPercent(
                    getRandom1To100(),
                    getRandom1To100(),
                ),
            )

            val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)
            seekbar.progress = ((seekbar.max - seekbar.min) * getRandom0_01To1_0()).toInt()

            onView(withId(R.id.edt_enter_geofence_name)).perform(typeText(geofenceName))
            onView(withId(R.id.btn_add_geofence_save)).perform(click())
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_geofence),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )

            onView(withId(R.id.rv_geofence)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(geofenceName)),
                ),
            )
        } else {
            rv.findViewHolderForAdapterPosition(0)?.let {
                val holder = it
                geofenceName =
                    holder.itemView.findViewById<AppCompatTextView>(R.id.tv_geofence_address_type).text.toString()
            }
        }
        val adapter = rv.adapter
        adapter?.let {
            initialCount = it.itemCount
        }

        initialCount = adapter?.itemCount
    }

    private fun deleteGeoFence() {
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName),
                ),
                clickOnViewChild(R.id.iv_delete_geofence),
            ),
        )
        onView(withText(mActivityRule.activity.getString(R.string.ok))).perform(click())

        uiDevice.wait(
            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/cl_search_loader_geofence_list")),
            DELAY_5000,
        )

        uiDevice.wait(
            Until.gone(By.res("${BuildConfig.APPLICATION_ID}:id/cl_search_loader_geofence_list")),
            DELAY_5000,
        )
        val iCount = initialCount
        if (iCount != null) {
            updatedCount = if (iCount > 1) {
                onView(withId(R.id.rv_geofence)).check(matches(isDisplayed()))
                val rv = mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_geofence)
                val adapter = rv.adapter
                adapter?.itemCount
            } else {
                onView(withId(R.id.cl_empty_geofence_list)).check(matches(isDisplayed()))
                0
            }
        } else {
            Assert.fail(TEST_FAILED)
        }
    }
}
