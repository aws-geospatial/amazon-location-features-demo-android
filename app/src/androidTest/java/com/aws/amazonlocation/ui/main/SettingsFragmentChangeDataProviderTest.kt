package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.json.JSONObject
import org.junit.*

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentChangeDataProviderTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)

        super.before()
    }

    @Test
    fun checkContent() {
        try {
            val esriMapName = mActivityRule.activity.getString(R.string.esri)
            val hereMapName = mActivityRule.activity.getString(R.string.here)

            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_3000)

            goToDataProvider()

            onView(
                allOf(withId(R.id.ll_here), isDisplayed()),
            ).perform(click())
            Thread.sleep(DELAY_3000)
            var selectedMapName = preferenceManager.getValue(KEY_MAP_NAME, esriMapName)

            if (selectedMapName != hereMapName) {
                Assert.fail(TEST_FAILED_SOURCE_NOT_HERE)
            }

            checkDataProvider(false)

            goToDataProvider()

            onView(
                allOf(withId(R.id.ll_esri), isDisplayed()),
            ).perform(click())
            Thread.sleep(DELAY_3000)
            selectedMapName = preferenceManager.getValue(KEY_MAP_NAME, esriMapName)

            if (selectedMapName != esriMapName) {
                Assert.fail(TEST_FAILED_SOURCE_NOT_ESRI)
            }

            checkDataProvider(true)
        } catch (e: Exception) {
            failTest(95, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToDataProvider() {
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
        onView(
            allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        )
            .perform(click())

        Thread.sleep(DELAY_1000)

        onView(
            allOf(
                withId(R.id.cl_data_provider),
                isDisplayed(),
            ),
        )
            .perform(click())
    }

    private fun checkDataProvider(checkEsri: Boolean) {
        val exploreTabText = mActivityRule.activity.getString(R.string.menu_explore)

        onView(
            allOf(
                withText(exploreTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        )
            .perform(click())

        Thread.sleep(DELAY_1000)

        var mapbox: MapboxMap? = null
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        Thread.sleep(DELAY_1000)

        var matchesWithRequested: Boolean

        ThreadUtils.runOnUiThread {
            matchesWithRequested = mapbox?.style?.json?.let {
                JSONObject(it).optJSONObject(
                    JSON_KEY_SOURCES,
                )?.has(if (checkEsri) JSON_KEY_ESRI else JSON_KEY_HERE) == true
            } == true
            if (!matchesWithRequested) {
                Assert.fail(TEST_FAILED_INCORRECT_SOURCE)
            }
        }
    }

    override fun after() {
        super.after()
        val targetContext = ApplicationProvider.getApplicationContext<Context>()
        val packageName = targetContext.packageName
        // Clear app from recent apps list
        val am = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.let {
            it.appTasks.forEach { task ->
                if (task.taskInfo.baseActivity?.packageName == packageName) {
                    task.finishAndRemoveTask()
                }
            }
        }
    }
}
