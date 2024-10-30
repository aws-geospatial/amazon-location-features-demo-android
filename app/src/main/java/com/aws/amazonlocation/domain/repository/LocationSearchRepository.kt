package com.aws.amazonlocation.domain.repository

import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import aws.sdk.kotlin.services.location.model.Step
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
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
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
        distanceInterface: DistanceInterface
    )

    suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    )
}
