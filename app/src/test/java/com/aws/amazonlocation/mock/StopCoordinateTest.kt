package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.Geometry
import com.aws.amazonlocation.data.response.Properties
import com.aws.amazonlocation.data.response.StopCoordinate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StopCoordinateTest {

    private lateinit var stopCoordinate: StopCoordinate

    @Before
    fun setUp() {
        stopCoordinate = StopCoordinate(
            geometry = Geometry(listOf(1.0, 2.0), POINT),
            id = 1,
            properties = Properties(2, STOP_123, BUS_STOP),
            type = FEATURE
        )
    }

    @Test
    fun testGeometry() {
        val expectedGeometry = Geometry(listOf(1.0, 2.0), POINT)
        assertEquals(expectedGeometry, stopCoordinate.geometry)
    }

    @Test
    fun testId() {
        val expectedId = 1
        assertEquals(expectedId, stopCoordinate.id)
    }

    @Test
    fun testProperties() {
        val expectedProperties = Properties(2, STOP_123, BUS_STOP)
        assertEquals(expectedProperties, stopCoordinate.properties)
    }

    @Test
    fun testType() {
        val expectedType = FEATURE
        assertEquals(expectedType, stopCoordinate.type)
    }
}