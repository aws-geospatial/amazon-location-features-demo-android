package com.aws.amazonlocation.utils.providers

import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.model.ListGeofencesResponse
import aws.smithy.kotlin.runtime.time.Instant
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimulationProvider {

    suspend fun getGeofenceList(
        collectionName: String,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): GeofenceData =
        try {
            val request =
                aws.sdk.kotlin.services.location.model.ListGeofencesRequest {
                    this.collectionName = collectionName
                }

            val response: ListGeofencesResponse? =
                withContext(Dispatchers.IO) {
                    locationClient?.listGeofences(request)
                }

            GeofenceData(
                geofenceList = ArrayList(response?.entries ?: emptyList()),
                message = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            GeofenceData(message = e.message)
        }

    suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        deviceId: String,
        identityId: String,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): UpdateBatchLocationResponse {
        val map: HashMap<String, String> = HashMap()
        identityId.split(":").let { splitStringList ->
            map["region"] = splitStringList[0]
            map["id"] = splitStringList[1]
        }

        val devicePositionUpdate =
            aws.sdk.kotlin.services.location.model.DevicePositionUpdate {
                position = position1
                this.deviceId = deviceId
                sampleTime = Instant.now()
                positionProperties = map
            }

        val request =
            aws.sdk.kotlin.services.location.model.BatchEvaluateGeofencesRequest {
                collectionName = trackerName
                devicePositionUpdates = listOf(devicePositionUpdate)
            }

        return try {
            withContext(Dispatchers.IO) {
                locationClient?.batchEvaluateGeofences(request)
            }
            UpdateBatchLocationResponse(null, true)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            UpdateBatchLocationResponse(e.message, false)
        }
    }
}
