package com.aws.amazonlocation.ui.main.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.services.geoplaces.model.GetPlaceResponse
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse
import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import aws.sdk.kotlin.services.georoutes.model.RouteTravelMode
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.CalculateDistanceResponse
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.utils.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class ExploreViewModel
    @Inject
    constructor(
        private var getLocationSearchUseCase: LocationSearchUseCase,
    ) : ViewModel() {
        var mLatLng: LatLng? = null
        var mStartLatLng: LatLng? = null
        var mDestinationLatLng: LatLng? = null
        var mIsPlaceSuggestion = true
        var mSearchSuggestionData: SearchSuggestionData? = null
        var mSearchDirectionOriginData: SearchSuggestionData? = null
        var mSearchDirectionDestinationData: SearchSuggestionData? = null
        var mCarData: CalculateRoutesResponse? = null
        var mWalkingData: CalculateRoutesResponse? = null
        var mTruckData: CalculateRoutesResponse? = null
        var mScooterData: CalculateRoutesResponse? = null
        var mNavigationResponse: NavigationResponse? = null
        private val mNavigationListModel = ArrayList<NavigationData>()
        var mStyleList = ArrayList<MapStyleData>()
        var mPoliticalData = ArrayList<PoliticalData>()
        var mPoliticalSearchData = ArrayList<PoliticalData>()

        private val _searchForSuggestionsResultList =
            Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
        val searchForSuggestionsResultList: Flow<HandleResult<SearchSuggestionResponse>> =
            _searchForSuggestionsResultList.receiveAsFlow()

        private val _searchLocationList =
            Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
        val mSearchLocationList: Flow<HandleResult<SearchSuggestionResponse>> =
            _searchLocationList.receiveAsFlow()

        private val _calculateDistance =
            Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
        val mCalculateDistance: Flow<HandleResult<CalculateDistanceResponse>> =
            _calculateDistance.receiveAsFlow()

        private val _updateCalculateDistance =
            Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
        val mUpdateCalculateDistance: Flow<HandleResult<CalculateDistanceResponse>> =
            _updateCalculateDistance.receiveAsFlow()

        private val _updateRoute =
            Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
        val mUpdateRoute: Flow<HandleResult<CalculateDistanceResponse>> =
            _updateRoute.receiveAsFlow()

        private val _navigationData =
            Channel<HandleResult<NavigationResponse>>(Channel.BUFFERED)
        val mNavigationData: Flow<HandleResult<NavigationResponse>> =
            _navigationData.receiveAsFlow()

        private val _addressLineData =
            Channel<HandleResult<SearchResponse>>(Channel.BUFFERED)
        val addressLineData: Flow<HandleResult<SearchResponse>> =
            _addressLineData.receiveAsFlow()

        private val _placeData =
            Channel<HandleResult<GetPlaceResponse>>(Channel.BUFFERED)
        val placeData: Flow<HandleResult<GetPlaceResponse>> =
            _placeData.receiveAsFlow()

        fun searchPlaceSuggestion(
            searchText: String,
        ) {
            _searchForSuggestionsResultList.trySend(
                HandleResult.Loading,
            )
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searchPlaceSuggestionList(
                    mLatLng?.latitude,
                    mLatLng?.longitude,
                    searchText,
                    object : SearchPlaceInterface {
                        override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                            _searchForSuggestionsResultList.trySend(
                                HandleResult.Success(
                                    suggestionResponse!!,
                                ),
                            )
                        }

                        override fun internetConnectionError(error: String) {
                            _searchForSuggestionsResultList.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(error),
                                ),
                            )
                        }
                    },
                )
            }
        }

        fun searchPlaceIndexForText(
            searchText: String? = null,
            queryId: String? = null,
        ) {
            _searchLocationList.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searchPlaceIndexForText(
                    mLatLng?.latitude,
                    mLatLng?.longitude,
                    searchText,
                    queryId,
                    object : SearchPlaceInterface {
                        override fun success(searchResponse: SearchSuggestionResponse) {
                            _searchLocationList.trySend(HandleResult.Success(searchResponse))
                        }

                        override fun error(searchResponse: SearchSuggestionResponse) {
                            searchResponse.error
                                ?.let {
                                    DataSourceException.Error(
                                        it,
                                    )
                                }?.let {
                                    HandleResult.Error(
                                        it,
                                    )
                                }?.let { _searchLocationList.trySend(it) }
                        }

                        override fun internetConnectionError(error: String) {
                            _searchLocationList.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        error,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }

        fun calculateDistance(
            latitude: Double?,
            longitude: Double?,
            latDestination: Double?,
            lngDestination: Double?,
            isAvoidFerries: Boolean?,
            isAvoidTolls: Boolean?,
            isWalkingAndTruckCall: Boolean,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                if (isWalkingAndTruckCall) {
                    val two =
                        async {
                            calculateDistanceFromMode(
                                latitude,
                                longitude,
                                latDestination,
                                lngDestination,
                                isAvoidFerries,
                                isAvoidTolls,
                                RouteTravelMode.Pedestrian.value,
                            )
                        }
                    two.await()
                    val three =
                        async {
                            calculateDistanceFromMode(
                                latitude,
                                longitude,
                                latDestination,
                                lngDestination,
                                isAvoidFerries,
                                isAvoidTolls,
                                RouteTravelMode.Truck.value,
                            )
                        }
                    three.await()
                    val four =
                        async {
                            calculateDistanceFromMode(
                                latitude,
                                longitude,
                                latDestination,
                                lngDestination,
                                isAvoidFerries,
                                isAvoidTolls,
                                RouteTravelMode.Scooter.value,
                            )
                        }
                    four.await()
                } else {
                    val one =
                        async {
                            calculateDistanceFromMode(
                                latitude,
                                longitude,
                                latDestination,
                                lngDestination,
                                isAvoidFerries,
                                isAvoidTolls,
                                RouteTravelMode.Car.value,
                            )
                        }
                    one.await()
                }
            }
        }

        private suspend fun calculateDistanceFromMode(
            latitude: Double?,
            longitude: Double?,
            latDestination: Double?,
            lngDestination: Double?,
            isAvoidFerries: Boolean?,
            isAvoidTolls: Boolean?,
            travelMode: String?,
        ) {
            _calculateDistance.trySend(HandleResult.Loading)
            mDestinationLatLng = lngDestination?.let { latDestination?.let { it1 -> LatLng(it1, it) } }
            mStartLatLng = longitude?.let { latitude?.let { it1 -> LatLng(it1, it) } }
            getLocationSearchUseCase.calculateRoute(
                latitude,
                longitude,
                latDestination,
                lngDestination,
                isAvoidFerries,
                isAvoidTolls,
                travelMode,
                object : DistanceInterface {
                    override fun distanceSuccess(success: CalculateRoutesResponse) {
                        _calculateDistance.trySend(
                            HandleResult.Success(
                                CalculateDistanceResponse(
                                    "$travelMode",
                                    success,
                                    sourceLatLng =
                                        if (latitude != null && longitude != null) {
                                            LatLng(latitude, longitude)
                                        } else {
                                            null
                                        },
                                    destinationLatLng =
                                        latDestination?.let {
                                            lngDestination?.let { it1 ->
                                                LatLng(
                                                    it,
                                                    it1,
                                                )
                                            }
                                        },
                                ),
                            ),
                        )
                    }

                    override fun distanceFailed(exception: DataSourceException) {
                        _calculateDistance.trySend(HandleResult.Error(exception))
                    }

                    override fun internetConnectionError(exception: String) {
                        _calculateDistance.trySend(
                            HandleResult.Error(
                                DataSourceException.Error(
                                    exception,
                                ),
                            ),
                        )
                    }
                },
            )
        }

        fun updateCalculateDistanceFromMode(
            latitude: Double?,
            longitude: Double?,
            latDestination: Double?,
            lngDestination: Double?,
            isAvoidFerries: Boolean?,
            isAvoidTolls: Boolean?,
            travelMode: String?,
        ) {
            _updateCalculateDistance.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.calculateRoute(
                    latitude,
                    longitude,
                    latDestination,
                    lngDestination,
                    isAvoidFerries,
                    isAvoidTolls,
                    travelMode,
                    object : DistanceInterface {
                        override fun distanceSuccess(success: CalculateRoutesResponse) {
                            _updateCalculateDistance.trySend(
                                HandleResult.Success(
                                    CalculateDistanceResponse(
                                        "$travelMode",
                                        success,
                                    ),
                                ),
                            )
                        }

                        override fun distanceFailed(exception: DataSourceException) {
                            _updateCalculateDistance.trySend(HandleResult.Error(exception))
                        }

                        override fun internetConnectionError(exception: String) {
                            _updateCalculateDistance.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        exception,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }

        fun calculateNavigationLine(
            context: Context,
            data: CalculateRoutesResponse,
        ) {
            _navigationData.trySend(HandleResult.Loading)
            data.routes[0].legs.let { legs ->
                mNavigationListModel.clear()
                viewModelScope.launch(Dispatchers.IO) {
                    mNavigationResponse = NavigationResponse()
                    mNavigationResponse?.duration =
                        data.routes[0]
                            .summary
                            ?.duration
                            ?.let { Units.getTime(context, it) }
                    mNavigationResponse?.distance =
                        data.routes[0]
                            .summary
                            ?.distance
                            ?.toDouble()
                    for (leg in legs) {
                        if (leg.vehicleLegDetails != null) {
                            leg.vehicleLegDetails?.travelSteps?.forEach {
                                mNavigationListModel.add(
                                    NavigationData(
                                        isDataSuccess = true,
                                        destinationAddress = it.instruction,
                                        distance = it.distance.toDouble(),
                                        duration = it.duration.toDouble(),
                                    ),
                                )
                            }
                        } else if (leg.pedestrianLegDetails != null) {
                            leg.pedestrianLegDetails?.travelSteps?.forEach {
                                mNavigationListModel.add(
                                    NavigationData(
                                        isDataSuccess = true,
                                        destinationAddress = it.instruction,
                                        distance = it.distance.toDouble(),
                                        duration = it.duration.toDouble(),
                                    ),
                                )
                            }
                        }else if (leg.ferryLegDetails != null) {
                            leg.ferryLegDetails?.travelSteps?.forEach {
                                mNavigationListModel.add(
                                    NavigationData(
                                        isDataSuccess = true,
                                        destinationAddress = it.instruction,
                                        distance = it.distance.toDouble(),
                                        duration = it.duration.toDouble(),
                                    ),
                                )
                            }
                        }
                    }
                    mNavigationResponse?.destinationAddress =
                        mSearchSuggestionData?.amazonLocationAddress?.label
                    mNavigationResponse?.navigationList = mNavigationListModel
                    mNavigationResponse?.let {
                        _navigationData.trySend(HandleResult.Success(it))
                    }
                }
            }
        }

        fun getAddressLineFromLatLng(
            longitude: Double?,
            latitude: Double?,
        ) {
            _addressLineData.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searPlaceIndexForPosition(
                    latitude,
                    longitude,
                    object : SearchDataInterface {
                        override fun getAddressData(reverseGeocodeResponse: ReverseGeocodeResponse) {
                            _addressLineData.trySend(
                                HandleResult.Success(
                                    SearchResponse(
                                        reverseGeocodeResponse,
                                        latitude,
                                        longitude,
                                    ),
                                ),
                            )
                        }

                        override fun error(error: String) {
                            _addressLineData.trySend(
                                HandleResult.Success(
                                    SearchResponse(null, latitude, longitude),
                                ),
                            )
                        }

                        override fun internetConnectionError(error: String) {
                            _addressLineData.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        error,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }

        fun getPlaceData(
            placeId: String
        ) {
            _placeData.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.getPlace(
                    placeId,
                    object : PlaceInterface {
                        override fun placeSuccess(success: GetPlaceResponse) {
                            _placeData.trySend(
                                HandleResult.Success(
                                    success,
                                ),
                            )
                        }

                        override fun placeFailed(exception: DataSourceException) {
                            _placeData.trySend(
                                HandleResult.Error(
                                    exception
                                ),
                            )
                        }

                        override fun internetConnectionError(exception: String) {
                            _placeData.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        exception,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }

        fun setMapListData(context: Context) {
            val items =
                arrayListOf(
                    MapStyleInnerData(
                        context.getString(R.string.map_standard),
                        false,
                        R.drawable.standard_light,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_monochrome),
                        false,
                        R.drawable.monochrome,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_hybrid),
                        false,
                        R.drawable.hybrid,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_satellite),
                        false,
                        R.drawable.satellite,
                    ),
                )
            mStyleList.clear()

            mStyleList =
                arrayListOf(
                    MapStyleData(
                        styleNameDisplay = "",
                        isSelected = false,
                        mapInnerData = items,
                    ),
                )
        }

    fun setPoliticalListData(context: Context) {
        val item = arrayListOf(
            PoliticalData(
                countryName = context.getString(R.string.label_arg),
                description = context.getString(R.string.description_arg),
                    countryCode = context.getString(R.string.flag_arg),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_egy),
                    description = context.getString(R.string.description_egy),
                    countryCode = context.getString(R.string.flag_egy),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ind),
                    description = context.getString(R.string.description_ind),
                    countryCode = context.getString(R.string.flag_ind),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ken),
                    description = context.getString(R.string.description_ken),
                    countryCode = context.getString(R.string.flag_ken),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_mar),
                    description = context.getString(R.string.description_mar),
                    countryCode = context.getString(R.string.flag_mar),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_rus),
                    description = context.getString(R.string.description_rus),
                    countryCode = context.getString(R.string.flag_rus),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sdn),
                    description = context.getString(R.string.description_sdn),
                    countryCode = context.getString(R.string.flag_sdn),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_srb),
                    description = context.getString(R.string.description_srb),
                    countryCode = context.getString(R.string.flag_srb),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sur),
                    description = context.getString(R.string.description_sur),
                    countryCode = context.getString(R.string.flag_sur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_syr),
                    description = context.getString(R.string.description_syr),
                    countryCode = context.getString(R.string.flag_syr),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tur),
                    description = context.getString(R.string.description_tur),
                    countryCode = context.getString(R.string.flag_tur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tza),
                    description = context.getString(R.string.description_tza),
                    countryCode = context.getString(R.string.flag_tza),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ury),
                    description = context.getString(R.string.description_ury),
                    countryCode = context.getString(R.string.flag_ury),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_vnm),
                    description = context.getString(R.string.description_vnm),
                    countryCode = context.getString(R.string.flag_vnm),
            )
        )
        mPoliticalData.addAll(item)

        mPoliticalSearchData.addAll(item)
    }

    fun searchPoliticalData(query: String): ArrayList<PoliticalData> {
        return ArrayList(mPoliticalSearchData.filter {
            it.countryName.contains(query, ignoreCase = true)
        })
    }
}
