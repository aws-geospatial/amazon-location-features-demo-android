package com.aws.amazonlocation.domain.repository

import com.aws.amazonlocation.ui.main.explore.AvoidanceOption
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface LocationSearchRepository {

    suspend fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface,
    )

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        queryId: String?,
        searchPlace: SearchPlaceInterface
    )

    suspend fun calculateDistance(
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

    suspend fun getPlace(placeId: String, placeInterface: PlaceInterface)
}
