package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.Geometry
import com.aws.amazonlocation.data.response.Properties
import com.aws.amazonlocation.data.response.RouteSimulationData
import com.aws.amazonlocation.data.response.RouteSimulationDataItem
import com.aws.amazonlocation.data.response.StopCoordinate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RouteSimulationDataItemTest {

    private lateinit var routeSimulationDataItem: RouteSimulationDataItem

    @Before
    fun setUp() {
        routeSimulationDataItem = RouteSimulationDataItem(
            coordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)),
            geofenceCollection = GEOFENCE_COLLECTION,
            id = ROUTE_ID,
            name = ROUTE_NAME,
            stopCoordinates = listOf(
                StopCoordinate(Geometry(listOf(1.0, 2.0), POINT), 1, Properties(1, STOP_123, BUS_STOP)),
                StopCoordinate(Geometry(listOf(3.0, 4.0), POINT), 2, Properties(2, STOP_456, BUS_STATION))
            )
        )
        routeSimulationDataItem.coordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        routeSimulationDataItem.geofenceCollection = GEOFENCE_COLLECTION
        routeSimulationDataItem.id = ROUTE_ID
        routeSimulationDataItem.name = ROUTE_NAME
        routeSimulationDataItem.stopCoordinates = listOf(
            StopCoordinate(Geometry(listOf(1.0, 2.0), POINT), 1, Properties(1, STOP_123, BUS_STOP)),
            StopCoordinate(Geometry(listOf(3.0, 4.0), POINT), 2, Properties(2, STOP_456,BUS_STATION))
        )
        routeSimulationDataItem.stopCoordinates?.let {
            it[0]?.properties?.id = 1
            it[0]?.properties?.stop_id = STOP_123
            it[0]?.properties?.stop_name = BUS_STOP
        }
        routeSimulationDataItem.stopCoordinates?.let {
            it[0]?.geometry?.type = it[0]?.geometry?.type
            it[0]?.geometry?.coordinates = it[0]?.geometry?.coordinates
        }
        val data = RouteSimulationData(arrayListOf(routeSimulationDataItem))
        data.busRoutesData = data.busRoutesData
    }

    @Test
    fun testCoordinates() {
        val expectedCoordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        assertEquals(expectedCoordinates, routeSimulationDataItem.coordinates)
    }

    @Test
    fun testGeofenceCollection() {
        val expectedGeofenceCollection = GEOFENCE_COLLECTION
        assertEquals(expectedGeofenceCollection, routeSimulationDataItem.geofenceCollection)
    }

    @Test
    fun testId() {
        val expectedId = ROUTE_ID
        assertEquals(expectedId, routeSimulationDataItem.id)
    }

    @Test
    fun testName() {
        val expectedName = ROUTE_NAME
        assertEquals(expectedName, routeSimulationDataItem.name)
    }

    @Test
    fun testStopCoordinates() {
        val expectedStopCoordinates = listOf(
            StopCoordinate(Geometry(listOf(1.0, 2.0), POINT), 1, Properties(1, STOP_123, BUS_STOP)),
            StopCoordinate(Geometry(listOf(3.0, 4.0), POINT), 2, Properties(2, STOP_456, BUS_STATION))
        )
        assertEquals(expectedStopCoordinates[0].properties?.id, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.id)
        assertEquals(expectedStopCoordinates[0].properties?.stop_name, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.stop_name)
        assertEquals(expectedStopCoordinates[0].properties?.stop_id, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.stop_id)
    }
}