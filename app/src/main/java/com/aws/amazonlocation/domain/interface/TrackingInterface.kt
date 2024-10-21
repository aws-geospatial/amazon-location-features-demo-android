package com.aws.amazonlocation.domain.`interface`

import java.util.Date
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface TrackingInterface {
    fun updateBatch(latLng: LatLng) {}
    fun updateBatch() {}
    fun removeUpdateBatch() {}
    fun getLocationHistory(startDate: Date, endDate: Date) {}
    fun getTodayLocationHistory(startDate: Date, endDate: Date) {}
    fun getGeofenceList(collectionName: String) {}
    fun getCheckPermission() {}
    fun getDeleteTrackingData() {}
}
