package com.aws.amazonlocation.data.datasource

import android.content.Context
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningRemoteDataSourceImplTest
import com.aws.amazonlocation.utils.providers.GeofenceProvider
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.providers.PlacesProvider
import com.aws.amazonlocation.utils.providers.RoutesProvider
import com.aws.amazonlocation.utils.providers.TrackingProvider
import com.aws.amazonlocation.utils.validateLatLng
import java.util.Date
import javax.inject.Inject
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class RemoteDataSourceImpl(
    var mContext: Context,
    var mLocationProvider: LocationProvider,
    var mPlacesProvider: PlacesProvider,
    var mRoutesProvider: RoutesProvider,
    var mGeofenceProvider: GeofenceProvider,
    var mTrackingProvider: TrackingProvider,
) : RemoteDataSource {
    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    override suspend fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface
    ) {
        if (mContext.isInternetAvailable()) {
            val mSearchSuggestionResponse =
                mPlacesProvider.searchPlaceSuggestion(lat, lng, searchText, mLocationProvider.getBaseActivity(), mLocationProvider.getGeoPlacesClient())
            if (validateLatLng(searchText) != null) {
                val mLatLng = validateLatLng(searchText)
                if (mSearchSuggestionResponse.text == (mLatLng?.latitude.toString() + "," + mLatLng?.longitude.toString())) {
                    searchPlace.getSearchPlaceSuggestionResponse(mSearchSuggestionResponse)
                } else {
                    if (!mSearchSuggestionResponse.error.isNullOrEmpty()) {
                        searchPlace.error(mSearchSuggestionResponse)
                    }
                }
            } else if (mSearchSuggestionResponse.text == searchText) {
                searchPlace.getSearchPlaceSuggestionResponse(mSearchSuggestionResponse)
            } else {
                if (!mSearchSuggestionResponse.error.isNullOrEmpty()) {
                    searchPlace.error(mSearchSuggestionResponse)
                }
            }
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        queryId: String?,
        searchPlace: SearchPlaceInterface,
    ) {
        if (mContext.isInternetAvailable()) {
            if (!searchText.isNullOrEmpty() || !queryId.isNullOrEmpty()) {
                val response =
                    mPlacesProvider.searchPlaceIndexForText(
                        lat = lat,
                        lng = lng,
                        mText = searchText, queryId, mLocationProvider.getBaseActivity(), mLocationProvider.getGeoPlacesClient()
                    )
                if (response?.text == searchText || !queryId.isNullOrEmpty()) {
                    if (response != null) {
                        searchPlace.success(response)
                    }
                } else {
                    if (!response?.error.isNullOrEmpty()) {
                        if (response != null) {
                            searchPlace.error(response)
                        }
                    }
                }
            }
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
        distanceInterface: DistanceInterface,
    ) {
        val calculateRoutesResponse = mRoutesProvider.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            travelMode,
            mLocationProvider.getBaseActivity(),
            mLocationProvider.getGeoRoutesClient()
        )

        if (mContext.isInternetAvailable()) {
            if (calculateRoutesResponse != null) {
                calculateRoutesResponse.let {
                    if (it.routes.isNotEmpty()) {
                        distanceInterface.distanceSuccess(it)
                    } else {
                        distanceInterface.distanceFailed(
                            DataSourceException.Error(
                                travelMode!!
                            )
                        )
                    }
                }
            } else {
                distanceInterface.distanceFailed(
                    DataSourceException.Error(
                        travelMode!!
                    )
                )
            }
        } else {
            distanceInterface.internetConnectionError(
                if (isRunningRemoteDataSourceImplTest) "" else mContext.resources.getString(
                    R.string.check_your_internet_connection_and_try_again
                )
            )
        }
    }

    override suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface,
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mPlacesProvider.searchNavigationPlaceIndexForPosition(lat, lng, mLocationProvider.getBaseActivity(), mLocationProvider.getGeoPlacesClient())
            if (indexResponse != null) {
                searchPlace.getAddressData(indexResponse)
            } else {
                searchPlace.error("")
            }
        } else {
            searchPlace.internetConnectionError(
                if (isRunningRemoteDataSourceImplTest) {
                    ""
                } else {
                    mContext.resources.getString(
                        R.string.check_your_internet_connection_and_try_again,
                    )
                },
            )
        }
    }

    override suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mGeofenceProvider.getGeofenceList(collectionName, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.getGeofenceList(response)
    }

    override suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mGeofenceProvider.addGeofence(geofenceId, collectionName, radius, latLng, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.addGeofence(response)
    }

    override suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mGeofenceProvider.deleteGeofence(position, data, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.deleteGeofence(response)
    }

    override suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        mTrackingInterface: BatchLocationUpdateInterface,
    ) {
        val response =
            mTrackingProvider.batchUpdateDevicePosition(trackerName, position, deviceId, mLocationProvider.getIdentityId(), mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        mTrackingInterface.success(response)
    }

    override suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>?,
        deviceId: String,
        identityId: String,
        mTrackingInterface: BatchLocationUpdateInterface,
    ) {
        val response =
            mGeofenceProvider.evaluateGeofence(trackerName, position1, deviceId, identityId, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        mTrackingInterface.success(response)
    }

    override suspend fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        historyInterface: LocationHistoryInterface,
    ) {
        val response =
            mTrackingProvider.getDevicePositionHistory(trackerName, deviceId, dateStart, dateEnd, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        historyInterface.success(response)
    }

    override suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface,
    ) {
        val response =
            mTrackingProvider.deleteDevicePositionHistory(trackerName, deviceId, mLocationProvider.getLocationClient(), mLocationProvider.getBaseActivity())
        historyInterface.success(response)
    }

    override suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface) {
        val response = mLocationProvider.fetchTokensWithOkHttp(authorizationCode)
        if (response != null) {
            signInInterface.fetchTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.fetchTokensWithOkHttpFailed("failed")
        }
    }

    override suspend fun refreshTokensWithOkHttp(signInInterface: SignInInterface) {
        val response = mLocationProvider.refreshTokensWithOkHttp()
        if (response != null) {
            signInInterface.refreshTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.refreshTokensWithOkHttpFailed("failed")
        }
    }
}
