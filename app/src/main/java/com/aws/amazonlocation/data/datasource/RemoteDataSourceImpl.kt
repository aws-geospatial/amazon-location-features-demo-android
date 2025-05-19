package com.aws.amazonlocation.data.datasource

import android.content.Context
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.ui.main.explore.AvoidanceOption
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningRemoteDataSourceImplTest
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.providers.PlacesProvider
import com.aws.amazonlocation.utils.providers.RoutesProvider
import com.aws.amazonlocation.utils.providers.SimulationProvider
import com.aws.amazonlocation.utils.validateLatLng
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class RemoteDataSourceImpl(
    var mContext: Context,
    var mLocationProvider: LocationProvider,
    var mPlacesProvider: PlacesProvider,
    var mRoutesProvider: RoutesProvider,
    var mGeofenceProvider: SimulationProvider
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
                mPlacesProvider.searchPlaceSuggestion(
                    lat,
                    lng,
                    searchText,
                    mLocationProvider.getBaseActivity(),
                    mLocationProvider.getGeoPlacesClient()
                )
            if (validateLatLng(searchText.trim()) != null) {
                val mLatLng = validateLatLng(searchText.trim())
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
            searchPlace.internetConnectionError(
                mContext.resources.getString(R.string.check_your_internet_connection_and_try_again)
            )
        }
    }

    override suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        queryId: String?,
        searchPlace: SearchPlaceInterface
    ) {
        if (mContext.isInternetAvailable()) {
            if (!searchText.isNullOrEmpty() || !queryId.isNullOrEmpty()) {
                val response =
                    mPlacesProvider.searchPlaceIndexForText(
                        lat = lat,
                        lng = lng,
                        mText = searchText,
                        queryId,
                        mLocationProvider.getBaseActivity(),
                        mLocationProvider.getGeoPlacesClient()
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
            searchPlace.internetConnectionError(
                mContext.resources.getString(R.string.check_your_internet_connection_and_try_again)
            )
        }
    }

    override suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        avoidanceOptions: ArrayList<AvoidanceOption>,
        departOption: String,
        travelMode: String?,
        time: String?,
        distanceInterface: DistanceInterface
    ) {
        val calculateRoutesResponse = mRoutesProvider.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            avoidanceOptions,
            departOption,
            travelMode,
            time,
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
                if (isRunningRemoteDataSourceImplTest) {
                    ""
                } else {
                    mContext.resources.getString(
                        R.string.check_your_internet_connection_and_try_again
                    )
                }
            )
        }
    }

    override suspend fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mPlacesProvider.searchNavigationPlaceIndexForPosition(
                lat,
                lng,
                mLocationProvider.getBaseActivity(),
                mLocationProvider.getGeoPlacesClient()
            )
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
                        R.string.check_your_internet_connection_and_try_again
                    )
                }
            )
        }
    }

    override suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        val response = mGeofenceProvider.getGeofenceList(
            collectionName,
            mLocationProvider.getLocationClient(),
            mLocationProvider.getBaseActivity()
        )
        mGeofenceAPIInterface.getGeofenceList(response)
    }

    override suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>?,
        deviceId: String,
        identityId: String,
        mTrackingInterface: BatchLocationUpdateInterface
    ) {
        val response =
            mGeofenceProvider.evaluateGeofence(
                trackerName,
                position1,
                deviceId,
                identityId,
                mLocationProvider.getLocationClient(),
                mLocationProvider.getBaseActivity()
            )
        mTrackingInterface.success(response)
    }

    override suspend fun getPlace(placeId: String, placeInterface: PlaceInterface) {
        if (mContext.isInternetAvailable()) {
            val placeResponse = mPlacesProvider.getPlace(
                placeId,
                mLocationProvider.getBaseActivity(),
                mLocationProvider.getGeoPlacesClient()
            )
            if (placeResponse != null) {
                placeInterface.placeSuccess(placeResponse)
            } else {
                placeInterface.placeFailed(DataSourceException.Error(""))
            }
        } else {
            placeInterface.internetConnectionError(
                if (isRunningRemoteDataSourceImplTest) {
                    ""
                } else {
                    mContext.resources.getString(
                        R.string.check_your_internet_connection_and_try_again
                    )
                }
            )
        }
    }
}
