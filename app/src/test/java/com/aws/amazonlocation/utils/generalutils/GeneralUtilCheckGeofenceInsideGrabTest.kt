package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.checkGeofenceInsideGrab
import com.aws.amazonlocation.utils.isRunningAnalyticsTest
import com.aws.amazonlocation.utils.isRunningRemoteDataSourceImplTest
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.isRunningTest2LiveLocation
import com.aws.amazonlocation.utils.isRunningTest3LiveLocation
import com.aws.amazonlocation.utils.isRunningTestLiveLocation
import com.aws.amazonlocation.utils.latSouth
import com.aws.amazonlocation.utils.px
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilCheckGeofenceInsideGrabTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun checkGeofenceInsideGrabTest() {
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningTest)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningTestLiveLocation)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningTest2LiveLocation)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningTest3LiveLocation)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningRemoteDataSourceImplTest)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, !isRunningAnalyticsTest)
        assertTrue(TEST_FAILED_GRAB_MAP_NOT_ENABLE, 9.px != null)
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, SE_REGION)
        var result =
            checkGeofenceInsideGrab(LatLng(latSouth, latSouth), mPreferenceManager, context)
        assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            result
        )
        mPreferenceManager.setValue(KEY_MAP_NAME, GRAB)
        result = checkGeofenceInsideGrab(LatLng(latSouth, latSouth), mPreferenceManager, context)
        assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            !result
        )
        result = checkGeofenceInsideGrab(LatLng(latSouth, latSouth), mPreferenceManager, null)
        assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            result
        )
    }

    @Test
    fun testIsGrabMapSelectedTrueAndInsideGeofence() {
        val latLng = LatLng(40.0, -75.0)
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, SE_REGION)

        val result = checkGeofenceInsideGrab(latLng, mPreferenceManager, context)

        assertTrue(result)
    }

    @Test
    fun testIsGrabMapSelectedTrueAndOutsideGeofence() {
        val latLng = LatLng(45.0, -80.0)
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, SE_REGION)

        val result = checkGeofenceInsideGrab(latLng, mPreferenceManager, context)

        assertTrue(result)
    }

    @Test
    fun testIsGrabMapSelectedFalse() {
        val latLng = LatLng(40.0, -75.0)
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, UNIT_AMPLIFY_JSON_CONF_INPUT_REGION)

        val result = checkGeofenceInsideGrab(latLng, mPreferenceManager, context)

        assertTrue(result)
    }

    @Test
    fun testContextNull() {
        val mContext: Context? = null
        val latLng = LatLng(40.0, -75.0)
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, UNIT_AMPLIFY_JSON_CONF_INPUT_REGION)
        val result = checkGeofenceInsideGrab(latLng, mPreferenceManager, mContext)

        assertTrue(result)
    }

    @Test
    fun testPreferenceManagerNull() {
        val latLng = LatLng(40.0, -75.0) // Inside or outside, doesn't matter

        val result = checkGeofenceInsideGrab(latLng, null, context)

        assertTrue(result)
    }

}
