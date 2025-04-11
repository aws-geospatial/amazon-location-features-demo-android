package com.aws.amazonlocation.mock

import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SimulationGeofenceDataTest {

    @Test
    fun testCollectionName() {
        val geofenceData = SimulationGeofenceData(
            collectionName = GEOFENCE_COLLECTION,
            devicePositionData = ArrayList()
        )
        geofenceData.collectionName = geofenceData.collectionName
        geofenceData.devicePositionData = geofenceData.devicePositionData
        assert(geofenceData.collectionName == GEOFENCE_COLLECTION)
    }

    @Test
    fun testDevicePositionData() {
        val devicePositionData = ArrayList<ListGeofenceResponseEntry>()
        val geofenceData = SimulationGeofenceData(
            collectionName = GEOFENCE_COLLECTION,
            devicePositionData = devicePositionData
        )
        assert(geofenceData.devicePositionData == devicePositionData)
    }
}
