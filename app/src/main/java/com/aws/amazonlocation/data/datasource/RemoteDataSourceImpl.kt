package com.aws.amazonlocation.data.datasource

import android.content.Context
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.sdk.kotlin.services.location.model.Step
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningRemoteDataSourceImplTest
import com.aws.amazonlocation.utils.validateLatLng
import java.util.Date
import javax.inject.Inject
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class RemoteDataSourceImpl(
    var mContext: Context,
    var mAWSLocationHelper: AWSLocationHelper,
) : RemoteDataSource {
    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    override suspend fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface,
        isGrabMapSelected: Boolean,
    ) {
        if (mContext.isInternetAvailable()) {
            val mSearchSuggestionResponse =
                mAWSLocationHelper.searchPlaceSuggestion(lat, lng, searchText, isGrabMapSelected)
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
        searchPlace: SearchPlaceInterface,
    ) {
        if (mContext.isInternetAvailable()) {
            if (!searchText.isNullOrEmpty()) {
                val response =
                    mAWSLocationHelper.searchPlaceIndexForText(
                        lat = lat,
                        lng = lng,
                        text = searchText,
                    )
                if (response.text == searchText) {
                    searchPlace.success(response)
                } else {
                    if (!response.error.isNullOrEmpty()) {
                        searchPlace.error(response)
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
        distanceType: String?,
        distanceInterface: DistanceInterface,
    ) {
        val mSearchSuggestionResponse = mAWSLocationHelper.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            distanceType
        )

        if (mContext.isInternetAvailable()) {
            if (mSearchSuggestionResponse != null) {
                mSearchSuggestionResponse.let {
                    if (it.summary != null) {
                        distanceInterface.distanceSuccess(it)
                    } else {
                        distanceInterface.distanceFailed(
                            DataSourceException.Error(
                                distanceType!!
                            )
                        )
                    }
                }
            } else {
                distanceInterface.distanceFailed(
                    DataSourceException.Error(
                        distanceType!!
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

    override suspend fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        step: Step,
        searchPlace: NavigationDataInterface,
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mAWSLocationHelper.searchNavigationPlaceIndexForPosition(lat, lng)
            val navigationData = NavigationData()
            navigationData.duration = step.durationSeconds
            navigationData.distance = step.distance
            navigationData.startLat = step.startPosition[1]
            navigationData.startLng = step.startPosition[0]
            navigationData.endLat = step.endPosition[1]
            navigationData.endLng = step.endPosition[0]
            if (!indexResponse?.results.isNullOrEmpty()) {
                navigationData.destinationAddress =
                    indexResponse
                        ?.results
                        ?.get(0)
                        ?.place
                        ?.label
                navigationData.region =
                    indexResponse
                        ?.results
                        ?.get(0)
                        ?.place
                        ?.region
                navigationData.subRegion =
                    indexResponse
                        ?.results
                        ?.get(0)
                        ?.place
                        ?.subRegion
                navigationData.country =
                    indexResponse
                        ?.results
                        ?.get(0)
                        ?.place
                        ?.country
                navigationData.isDataSuccess = true
            } else {
                navigationData.destinationAddress = "$lat, $lng"
                navigationData.isDataSuccess = false
            }
            searchPlace.getNavigationList(navigationData)
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

    override suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface,
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mAWSLocationHelper.searchNavigationPlaceIndexForPosition(lat, lng)
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
        val response = mAWSLocationHelper.getGeofenceList(collectionName)
        mGeofenceAPIInterface.getGeofenceList(response)
    }

    override suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mAWSLocationHelper.addGeofence(geofenceId, collectionName, radius, latLng)
        mGeofenceAPIInterface.addGeofence(response)
    }

    override suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface,
    ) {
        val response = mAWSLocationHelper.deleteGeofence(position, data)
        mGeofenceAPIInterface.deleteGeofence(response)
    }

    override suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        mTrackingInterface: BatchLocationUpdateInterface,
    ) {
        val response =
            mAWSLocationHelper.batchUpdateDevicePosition(trackerName, position, deviceId)
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
            mAWSLocationHelper.evaluateGeofence(trackerName, position1, deviceId, identityId)
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
            mAWSLocationHelper.getDevicePositionHistory(trackerName, deviceId, dateStart, dateEnd)
        historyInterface.success(response)
    }

    override suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface,
    ) {
        val response =
            mAWSLocationHelper.deleteDevicePositionHistory(trackerName, deviceId)
        historyInterface.success(response)
    }

    override suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface) {
        val response = mAWSLocationHelper.fetchTokensWithOkHttp(authorizationCode)
        if (response != null) {
            signInInterface.fetchTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.fetchTokensWithOkHttpFailed("failed")
        }
    }

    override suspend fun refreshTokensWithOkHttp(signInInterface: SignInInterface) {
        val response = mAWSLocationHelper.refreshTokensWithOkHttp()
        if (response != null) {
            signInInterface.refreshTokensWithOkHttpSuccess("success", response)
        } else {
            signInInterface.refreshTokensWithOkHttpFailed("failed")
        }
    }
}
