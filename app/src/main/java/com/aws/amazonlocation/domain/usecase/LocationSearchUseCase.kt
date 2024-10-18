package com.aws.amazonlocation.domain.usecase

import aws.sdk.kotlin.services.location.model.Step
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.repository.LocationSearchRepository
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class LocationSearchUseCase @Inject constructor(private val mLocationSearchRepository: LocationSearchRepository) {

    suspend fun searchPlaceSuggestionList(
        lat: Double?,
        lng: Double?,
        searchText: String,
        isGrabMapSelected: Boolean,
        searchPlace: SearchPlaceInterface
    ) =
        mLocationSearchRepository.searchPlaceSuggestions(lat, lng, searchText, searchPlace, isGrabMapSelected)

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    ) = mLocationSearchRepository.searchPlaceIndexForText(lat, lng, searchText, searchPlace)

    suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
        distanceInterface: DistanceInterface
    ) = mLocationSearchRepository.calculateDistance(
        latDeparture,
        lngDeparture,
        latDestination,
        lngDestination,
        isAvoidFerries,
        isAvoidTolls,
        travelMode,
        distanceInterface
    )

    suspend fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        step: Step,
        searchPlace: NavigationDataInterface
    ) = mLocationSearchRepository.searchNavigationPlaceIndexForPosition(lat, lng, step, searchPlace)

    suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    ) = mLocationSearchRepository.searPlaceIndexForPosition(lat, lng, searchPlace)
}
