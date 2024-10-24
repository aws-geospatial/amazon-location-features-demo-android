package com.aws.amazonlocation.ui.main

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_4000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_INVALID_IDENTITY_POOL_ID
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_CAPS
import com.aws.amazonlocation.WHILE_USING_THE_APP_ALLOW
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.scrollForView
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitUntil
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingAwsConnectTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showAwsConnectTest() {
        try {
            Thread.sleep(DELAY_4000)
            enableGPS(ApplicationProvider.getApplicationContext())
            val btnContinueToApp = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
                Thread.sleep(DELAY_2000)
                try {
                    Thread.sleep(DELAY_2000)
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
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
            }
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val tracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
                DELAY_1000,
            )

            val clEnableTracking =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_enable_tracking)
            if (clEnableTracking.visibility == View.VISIBLE) {
                val btnTryTracker =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_enable_tracking)
                mActivityRule.activity.runOnUiThread {
                    btnTryTracker.performClick()
                }
            }
            Thread.sleep(DELAY_1000)
            val appViews = UiScrollable(UiSelector().scrollable(true))

            val edtIdentityPoolId =
                scrollForView(allOf(withId(R.id.edt_identity_pool_id), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtIdentityPoolId?.perform(replaceText(BuildConfig.IDENTITY_POOL_ID))

            val edtUserDomain =
                scrollForView(allOf(withId(R.id.edt_user_domain), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtUserDomain?.perform(replaceText(BuildConfig.USER_DOMAIN))

            val edtUserPoolClientId =
                scrollForView(
                    allOf(
                        withId(R.id.edt_user_pool_client_id),
                        isCompletelyDisplayed(),
                    ),
                ) {
                    appViews.scrollForward(2)
                }
            edtUserPoolClientId?.perform(replaceText(BuildConfig.USER_POOL_CLIENT_ID))

            val edtUserPoolId =
                scrollForView(allOf(withId(R.id.edt_user_pool_id), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtUserPoolId?.perform(replaceText(BuildConfig.USER_POOL_ID))

            val edtWebSocketUrl =
                scrollForView(allOf(withId(R.id.edt_web_socket_url), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtWebSocketUrl?.perform(replaceText(BuildConfig.WEB_SOCKET_URL))

            val btnConnect =
                onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
            btnConnect.perform(click())
            Thread.sleep(DELAY_5000)
//            uiDevice.wait(
//                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.you_are_connected))),
//                DELAY_5000,
//            )
            val targetContext: Context = getInstrumentation().targetContext.applicationContext
            val pm = PreferenceManager(targetContext)
            val mPoolId = pm.getValue(KEY_POOL_ID, "")
            Assert.assertTrue(
                TEST_FAILED_INVALID_IDENTITY_POOL_ID,
                mPoolId == BuildConfig.IDENTITY_POOL_ID,
            )
        } catch (e: Exception) {
            failTest(90, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
