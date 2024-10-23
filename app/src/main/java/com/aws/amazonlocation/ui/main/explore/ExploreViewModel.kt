package com.aws.amazonlocation.ui.main.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

                    if (legs[0].vehicleLegDetails != null) {
                        legs[0].vehicleLegDetails?.travelSteps?.forEach {
                            mNavigationListModel.add(
                                NavigationData(
                                    isDataSuccess = true,
                                    destinationAddress = it.instruction,
                                    distance = it.distance.toDouble(),
                                    duration = it.duration.toDouble(),
                                ),
                            )
                        }
                        mNavigationResponse?.startLat =
                            data.routes[0].legs[0].vehicleLegDetails?.departure?.place?.originalPosition?.get(
                                1,
                            )
                        mNavigationResponse?.startLng =
                            data.routes[0].legs[0].vehicleLegDetails?.departure?.place?.originalPosition?.get(
                                0,
                            )
                        mNavigationResponse?.endLat =
                            data.routes[0].legs[0].vehicleLegDetails?.arrival?.place?.originalPosition?.get(
                                1,
                            )
                        mNavigationResponse?.endLng =
                            data.routes[0].legs[0].vehicleLegDetails?.arrival?.place?.originalPosition?.get(
                                0,
                            )
                    } else if (legs[0].pedestrianLegDetails != null) {
                        legs[0].pedestrianLegDetails?.travelSteps?.forEach {
                            mNavigationListModel.add(
                                NavigationData(
                                    isDataSuccess = true,
                                    destinationAddress = it.instruction,
                                    distance = it.distance.toDouble(),
                                    duration = it.duration.toDouble(),
                                ),
                            )
                        }
                        mNavigationResponse?.startLat =
                            data.routes[0].legs[0].pedestrianLegDetails?.departure?.place?.originalPosition?.get(
                                1,
                            )
                        mNavigationResponse?.startLng =
                            data.routes[0].legs[0].pedestrianLegDetails?.departure?.place?.originalPosition?.get(
                                0,
                            )
                        mNavigationResponse?.endLat =
                            data.routes[0].legs[0].pedestrianLegDetails?.arrival?.place?.originalPosition?.get(
                                1,
                            )
                        mNavigationResponse?.endLng =
                            data.routes[0].legs[0].pedestrianLegDetails?.arrival?.place?.originalPosition?.get(
                                0,
                            )
                    }
                    mNavigationResponse?.destinationAddress =
                        mSearchSuggestionData?.amazonLocationPlace?.label
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

        fun setMapListData(context: Context) {
            val items =
                arrayListOf(
                    MapStyleInnerData(
                        context.getString(R.string.map_standard),
                        false,
                        R.drawable.light,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_monochrome),
                        false,
                        R.drawable.streets,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_hybrid),
                        false,
                        R.drawable.navigation,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_satellite),
                        false,
                        R.drawable.dark_gray,
                    ),
                )
            mStyleList.clear()

            mStyleList =
                arrayListOf(
                    MapStyleData(
                        styleNameDisplay = "",
                        isSelected = false, // Set isSelected as per your requirement
                        mapInnerData = items,
                    ),
                )
        }

    fun setPoliticalListData(context: Context) {
        val item = arrayListOf(
            PoliticalData(
                countryName = context.getString(R.string.label_arg),
                description = "Argentina's view on the Southern Patagonian Ice Field and Tierra Del Fuego, including the Falkland Islands, South Georgia, and South Sandwich Islands",
                countryCode = context.getString(R.string.flag_arg),
            ),
            PoliticalData(
                countryName = "EGY",
                description = "Egypt's view on Bir Tawil",
                countryCode = context.getString(R.string.flag_egy),
            ),
            PoliticalData(
                countryName = "IND",
                description = "India's view on Gilgit-Baltistan",
                countryCode = context.getString(R.string.flag_ind),
            ),
            PoliticalData(
                countryName = "KEN",
                description = "Kenya's view on the Ilemi Triangle",
                countryCode = context.getString(R.string.flag_ken),
            ),
            PoliticalData(
                countryName = "MAR",
                description = "Morocco's view on Western Sahara",
                countryCode = context.getString(R.string.flag_mar),
            ),
            PoliticalData(
                countryName = "PAK",
                description = "Pakistan's view on Jammu and Kashmir and the Junagadh Area",
                countryCode = context.getString(R.string.flag_pak),
            ),
            PoliticalData(
                countryName = "RUS",
                description = "Russia's view on Crimea",
                countryCode = context.getString(R.string.flag_rus),
            ),
            PoliticalData(
                countryName = "SDN",
                description = "Sudan's view on the Halaib Triangle",
                countryCode = context.getString(R.string.flag_sdn),
            ),
            PoliticalData(
                countryName = "SRB",
                description = "Serbia's view on Kosovo, Vukovar, and Sarengrad Islands",
                countryCode = context.getString(R.string.flag_srb),
            ),
            PoliticalData(
                countryName = "SUR",
                description = "Suriname's view on the Courantyne Headwaters and Lawa Headwaters",
                countryCode = context.getString(R.string.flag_sur),
            ),
            PoliticalData(
                countryName = "SYR",
                description = "Syria's view on the Golan Heights",
                countryCode = context.getString(R.string.flag_syr),
            ),
            PoliticalData(
                countryName = "TUR",
                description = "Turkey's view on Cyprus and Northern Cyprus",
                countryCode = context.getString(R.string.flag_tur),
            ),
            PoliticalData(
                countryName = "TZA",
                description = "Tanzania's view on Lake Malawi",
                countryCode = context.getString(R.string.flag_tza),
            ),
            PoliticalData(
                countryName = "URY",
                description = "Uruguay's view on Rincon de Artigas",
                countryCode = context.getString(R.string.flag_ury),
            ),
            PoliticalData(
                countryName = "VNM",
                description = "Vietnam's view on the Paracel Islands and Spratly Islands",
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
