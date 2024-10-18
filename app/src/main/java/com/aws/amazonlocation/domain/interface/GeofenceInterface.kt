package com.aws.amazonlocation.domain.`interface`

import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.enum.GeofenceBottomSheetEnum
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface GeofenceInterface {
    fun addGeofence(geofenceId: String, collectionName: String, radius: Double?, latLng: LatLng?) {}
    fun getGeofenceList(collectionName: String) {}
    fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry) {}
    fun geofenceSearchPlaceIndexForText(searchText: String) {}
    fun hideShowBottomNavigationBar(isHide: Boolean = false, type: GeofenceBottomSheetEnum)
    fun openAddGeofenceBottomSheet(point: LatLng)
}
