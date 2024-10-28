package com.aws.amazonlocation.utils.providers

import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.model.Circle
import aws.sdk.kotlin.services.location.model.GeofenceGeometry
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.sdk.kotlin.services.location.model.ListGeofencesResponse
import aws.sdk.kotlin.services.location.model.PutGeofenceRequest
import aws.smithy.kotlin.runtime.time.Instant
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.GeofenceCons.GEOFENCE_COLLECTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

class GeofenceProvider {

    suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): AddGeofenceResponse {
        val putGeofenceRequest =
            PutGeofenceRequest {
                this.collectionName = collectionName
                this.geofenceId = geofenceId
                geometry =
                    GeofenceGeometry {
                        circle =
                            Circle {
                                center =
                                    latLng?.let {
                                        listOf(it.longitude, it.latitude)
                                    }
                                this.radius = radius
                            }
                    }
            }

        return try {
            locationClient?.putGeofence(putGeofenceRequest)
            AddGeofenceResponse(isGeofenceDataAdded = true, errorMessage = null)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            AddGeofenceResponse(isGeofenceDataAdded = false, errorMessage = e.message)
        }
    }

    suspend fun getGeofenceList(
        collectionName: String,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?): GeofenceData =
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
                message = null,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            GeofenceData(message = e.message)
        }

    suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        locationClient: LocationClient?,
        mBaseActivity: BaseActivity?
    ): DeleteGeofence {
        val batchDeleteGeofenceRequest =
            aws.sdk.kotlin.services.location.model.BatchDeleteGeofenceRequest {
                collectionName = GEOFENCE_COLLECTION
                geofenceIds = listOf(data.geofenceId)
            }

        return try {
            locationClient?.batchDeleteGeofence(batchDeleteGeofenceRequest)
            DeleteGeofence(data = data, position = position)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteGeofence(data = null, errorMessage = e.message)
        }
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
