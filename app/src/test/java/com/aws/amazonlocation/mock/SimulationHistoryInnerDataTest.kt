package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.SimulationHistoryInnerData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class SimulationHistoryInnerDataTest {

    @Test
    fun testLatitude() {
        val simulationData = SimulationHistoryInnerData(latitude = 42.0, longitude = 24.0, receivedTime = Date())
        assert(simulationData.latitude == 42.0)
    }

    @Test
    fun testLongitude() {
        val simulationData = SimulationHistoryInnerData(latitude = 42.0, longitude = 24.0, receivedTime = Date())
        assert(simulationData.longitude == 24.0)
    }

    @Test
    fun testReceivedTime() {
        val receivedTime = Date()
        val simulationData = SimulationHistoryInnerData(latitude = 42.0, longitude = 24.0, receivedTime = receivedTime)
        assert(simulationData.receivedTime == receivedTime)
    }
}
