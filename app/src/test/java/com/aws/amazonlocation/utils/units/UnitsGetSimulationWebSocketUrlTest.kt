package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsGetSimulationWebSocketUrlTest : BaseTest() {

    @Test
    fun checkSimulationWebSocketUrlSuccess() {
        var result = Units.getSimulationWebSocketUrl(BuildConfig.DEFAULT_IDENTITY_POOL_ID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.SIMULATION_WEB_SOCKET_URL)
        result = Units.getSimulationWebSocketUrl(BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.SIMULATION_WEB_SOCKET_URL_EU)
        result = Units.getSimulationWebSocketUrl("")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.SIMULATION_WEB_SOCKET_URL)
    }
}
