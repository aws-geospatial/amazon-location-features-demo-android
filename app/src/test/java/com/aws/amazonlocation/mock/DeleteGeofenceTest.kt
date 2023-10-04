package com.aws.amazonlocation.mock

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.response.DeleteGeofence
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteGeofenceTest {

    @Test
    fun testPosition() {
        val deleteGeofence = DeleteGeofence(position = 42, data = ListGeofenceResponseEntry(), errorMessage = null)
        deleteGeofence.position = deleteGeofence.position
        deleteGeofence.data = deleteGeofence.data
        deleteGeofence.errorMessage = deleteGeofence.errorMessage
        assert(deleteGeofence.position == 42)
    }
}
