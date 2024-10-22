package com.aws.amazonlocation.utils.units

import android.provider.Settings
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UnitsGetDeviceIdTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun getDeviceIdSuccess() {
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, DEVICE_ID)
        val result = Units.getDeviceId(context)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == DEVICE_ID)
    }
}
