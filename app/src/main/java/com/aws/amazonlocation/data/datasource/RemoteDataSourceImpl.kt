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
    var mAWSLocationProvider: LocationProvider,
    var mAWSPlacesProvider: PlacesProvider,
    var mAWSRoutesProvider: RoutesProvider,
    var mAWSGeofenceProvider: GeofenceProvider,
    var mAWSTrackingProvider: TrackingProvider,
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
                mAWSPlacesProvider.searchPlaceSuggestion(lat, lng, searchText, mAWSLocationProvider.getBaseActivity(), mAWSLocationProvider.getGeoPlacesClient())
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
                    mAWSPlacesProvider.searchPlaceIndexForText(
                        lat = lat,
                        lng = lng,
                        mText = searchText, queryId, mAWSLocationProvider.getBaseActivity(), mAWSLocationProvider.getGeoPlacesClient()
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
        val calculateRoutesResponse = mAWSRoutesProvider.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            travelMode,
            mAWSLocationProvider.getBaseActivity(),
            mAWSLocationProvider.getGeoRoutesClient()
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
            val indexResponse = mAWSPlacesProvider.searchNavigationPlaceIndexForPosition(lat, lng, mAWSLocationProvider.getBaseActivity(), mAWSLocationProvider.getGeoPlacesClient())
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
        val response = mAWSGeofenceProvider.getGeofenceList(collectionName, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.getGeofenceList(response)
    }

    override suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mAWSGeofenceProvider.addGeofence(geofenceId, collectionName, radius, latLng, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.addGeofence(response)
    }

    override suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mAWSGeofenceProvider.deleteGeofence(position, data, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
        mGeofenceAPIInterface.deleteGeofence(response)
    }

    override suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        mTrackingInterface: BatchLocationUpdateInterface,
    ) {
        val response =
            mAWSTrackingProvider.batchUpdateDevicePosition(trackerName, position, deviceId, mAWSLocationProvider.getIdentityId(), mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
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
            mAWSGeofenceProvider.evaluateGeofence(trackerName, position1, deviceId, identityId, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
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
            mAWSTrackingProvider.getDevicePositionHistory(trackerName, deviceId, dateStart, dateEnd, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
        historyInterface.success(response)
    }

    override suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface,
    ) {
        val response =
            mAWSTrackingProvider.deleteDevicePositionHistory(trackerName, deviceId, mAWSLocationProvider.getLocationClient(), mAWSLocationProvider.getBaseActivity())
        historyInterface.success(response)
    }

    override suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface) {
        val response = mAWSLocationProvider.fetchTokensWithOkHttp(authorizationCode)
        if (response != null) {
            signInInterface.fetchTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.fetchTokensWithOkHttpFailed("failed")
        }
    }

    override suspend fun refreshTokensWithOkHttp(signInInterface: SignInInterface) {
        val response = mAWSLocationProvider.refreshTokensWithOkHttp()
        if (response != null) {
            signInInterface.refreshTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.refreshTokensWithOkHttpFailed("failed")
        }
    }
}
