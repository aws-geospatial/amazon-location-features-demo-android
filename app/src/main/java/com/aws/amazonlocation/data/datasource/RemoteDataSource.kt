package com.aws.amazonlocation.data.datasource

import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.ui.main.explore.AvoidanceOption

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface RemoteDataSource {

    suspend fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface
    )

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        queryId: String?,
        searchPlace: SearchPlaceInterface
    )

    suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        avoidanceOptions: ArrayList<AvoidanceOption>,
        departOption: String,
        travelMode: String?,
        time: String?,
        distanceInterface: DistanceInterface
    )

    suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    )

    suspend fun getGeofenceList(collectionName: String, mGeofenceAPIInterface: GeofenceAPIInterface)

    suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        deviceId: String,
        identityId: String,
        mTrackingInterface: BatchLocationUpdateInterface
    )

    suspend fun getPlace(
        placeId: String,
        placeInterface: PlaceInterface
    )
}
