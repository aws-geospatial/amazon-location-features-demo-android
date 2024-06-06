package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.BusData
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class BusDataTest {

    @Test
    fun testCurrentPosition() {
        val busData = BusData(
            currentPosition = LatLng(1.0, 2.0),
            geoJsonSource = null,
            animator =null
        )
        busData.currentPosition = LatLng(1.0, 2.0)
        busData.geoJsonSource = null
        busData.animator = null
        val expectedLatLng = LatLng(1.0, 2.0)
        assertEquals(expectedLatLng, busData.currentPosition)
    }
}