package com.aws.amazonlocation.mock

import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.smithy.kotlin.runtime.time.Instant
import com.aws.amazonlocation.data.response.DeleteGeofence
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteGeofenceTest {

    @Test
    fun testPosition() {
        val deleteGeofence = DeleteGeofence(position = 42, data = ListGeofenceResponseEntry{
            geofenceId = "test"
            createTime = Instant.now()
            updateTime = Instant.now()
            status = "test"
        }, errorMessage = null)
        assert(deleteGeofence.data?.geofenceId == "test")
    }
}