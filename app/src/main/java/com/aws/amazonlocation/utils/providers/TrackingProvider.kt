package com.aws.amazonlocation.utils.providers

import aws.sdk.kotlin.services.location.LocationClient
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import java.util.Date

class TrackingProvider {
    suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        identityId: String?,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): UpdateBatchLocationResponse {
        if (identityId == null) {
            UpdateBatchLocationResponse("Identity is is null", false)
        }
        val map: MutableMap<String, String> =
            identityId!!.split(":").let { splitStringList ->
                mutableMapOf(
                    "region" to splitStringList[0],
                    "id" to splitStringList[1],
                )
            }

        val devicePositionUpdate =
            aws.sdk.kotlin.services.location.model.DevicePositionUpdate {
                this.position = position
                this.deviceId = deviceId
                this.sampleTime = Instant.now()
                this.positionProperties = map
            }

        val request =
            aws.sdk.kotlin.services.location.model.BatchUpdateDevicePositionRequest {
                this.trackerName = trackerName
                this.updates = listOf(devicePositionUpdate)
            }
        return try {
            locationClient?.batchUpdateDevicePosition(request)

            UpdateBatchLocationResponse(null, true)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            UpdateBatchLocationResponse(e.message, true)
        }
    }

    suspend fun getDevicePositionHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): LocationHistoryResponse {
        val request =
            aws.sdk.kotlin.services.location.model.GetDevicePositionHistoryRequest {
                this.trackerName = trackerName
                this.deviceId = deviceId
                this.startTimeInclusive = Instant.fromEpochMilliseconds(dateStart.time)
                this.endTimeExclusive = Instant.fromEpochMilliseconds(dateEnd.time)
            }
        return try {
            val response = locationClient?.getDevicePositionHistory(request)
            LocationHistoryResponse(null, response)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            LocationHistoryResponse(e.message, null)
        }
    }

    suspend fun deleteDevicePositionHistory(
        trackerName: String,
        deviceId: String,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): DeleteLocationHistoryResponse =
        try {
            val request =
                aws.sdk.kotlin.services.location.model.BatchDeleteDevicePositionHistoryRequest {
                    this.trackerName = trackerName
                    this.deviceIds = listOf(deviceId)
                }
            val response = locationClient?.batchDeleteDevicePositionHistory(request)
            DeleteLocationHistoryResponse(null, response)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteLocationHistoryResponse(e.message, null)
        }
}
