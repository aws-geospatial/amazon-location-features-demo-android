package com.aws.amazonlocation.data.repository

import aws.sdk.kotlin.services.location.model.Step
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.repository.LocationSearchRepository

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class LocationSearchImp(private val mRemoteDataSource: RemoteDataSourceImpl) :
    LocationSearchRepository {

    override suspend fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface,
        isGrabMapSelected: Boolean
    ) {
        mRemoteDataSource.searchPlaceSuggestions(lat, lng, searchText, searchPlace, isGrabMapSelected)
    }

    override suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    ) {
        mRemoteDataSource.searchPlaceIndexForText(lat, lng, searchText, searchPlace)
    }

    override suspend fun calculateDistance(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
        distanceInterface: DistanceInterface
    ) {
        mRemoteDataSource.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            travelMode,
            distanceInterface
        )
    }

    override suspend fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        step: Step,
        searchPlace: NavigationDataInterface
    ) {
        mRemoteDataSource.searchNavigationPlaceIndexForPosition(
            lat,
            lng,
            step,
            searchPlace
        )
    }

    override suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    ) {
        mRemoteDataSource.searPlaceIndexForPosition(
            lat,
            lng,
            searchPlace
        )
    }
}
