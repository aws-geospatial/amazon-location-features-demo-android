package com.aws.amazonlocation.domain.usecase

import com.aws.amazonlocation.ui.main.explore.AvoidanceOption
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
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
        searchPlace: SearchPlaceInterface
    ) =
        mLocationSearchRepository.searchPlaceSuggestions(lat, lng, searchText, searchPlace)

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        queryId: String?,
        searchPlace: SearchPlaceInterface
    ) = mLocationSearchRepository.searchPlaceIndexForText(lat, lng, searchText, queryId, searchPlace)

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
    ) = mLocationSearchRepository.
    calculateDistance(
        latDeparture,
        lngDeparture,
        latDestination,
        lngDestination,
        avoidanceOptions,
        departOption,
        travelMode,
        time,
        distanceInterface
    )

    suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    ) = mLocationSearchRepository.searPlaceIndexForPosition(lat, lng, searchPlace)

    suspend fun getPlace(placeId: String, placeInterface: PlaceInterface) = mLocationSearchRepository.getPlace(placeId, placeInterface)
}
