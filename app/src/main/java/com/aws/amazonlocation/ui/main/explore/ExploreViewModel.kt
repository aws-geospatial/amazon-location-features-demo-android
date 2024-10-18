package com.aws.amazonlocation.ui.main.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.services.location.model.CalculateRouteResponse
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse
import aws.sdk.kotlin.services.location.model.Step
import aws.sdk.kotlin.services.location.model.TravelMode
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.CalculateDistanceResponse
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.utils.ATTRIBUTE_3D
import com.aws.amazonlocation.utils.ATTRIBUTE_DARK
import com.aws.amazonlocation.utils.ATTRIBUTE_LIGHT
import com.aws.amazonlocation.utils.ATTRIBUTE_SATELLITE
import com.aws.amazonlocation.utils.ATTRIBUTE_TRUCK
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapStyles
import com.aws.amazonlocation.utils.TRAVEL_MODE_BICYCLE
import com.aws.amazonlocation.utils.TRAVEL_MODE_MOTORCYCLE
import com.aws.amazonlocation.utils.TYPE_RASTER
import com.aws.amazonlocation.utils.TYPE_VECTOR
import com.aws.amazonlocation.utils.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject

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
        var mCarData: CalculateRouteResponse? = null
        var mWalkingData: CalculateRouteResponse? = null
        var mTruckData: CalculateRouteResponse? = null
        var mBicycleData: CalculateRouteResponse? = null
        var mMotorcycleData: CalculateRouteResponse? = null
        var mNavigationResponse: NavigationResponse? = null
        private val mNavigationListModel = ArrayList<NavigationData>()
        var mStyleList = ArrayList<MapStyleData>()
        var mStyleListForFilter = ArrayList<MapStyleData>()
        var providerOptions = ArrayList<FilterOption>()
        var attributeOptions = ArrayList<FilterOption>()
        var typeOptions = ArrayList<FilterOption>()

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

        private val _navigationTimeDialogData =
            Channel<HandleResult<NavigationData>>(Channel.BUFFERED)
        val mNavigationTimeDialogData: Flow<HandleResult<NavigationData>> =
            _navigationTimeDialogData.receiveAsFlow()

        private val _addressLineData =
            Channel<HandleResult<SearchResponse>>(Channel.BUFFERED)
        val addressLineData: Flow<HandleResult<SearchResponse>> =
            _addressLineData.receiveAsFlow()

        fun searchPlaceSuggestion(
            searchText: String,
            isGrabMapSelected: Boolean,
        ) {
            _searchForSuggestionsResultList.trySend(
                HandleResult.Loading,
            )
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searchPlaceSuggestionList(
                    mLatLng?.latitude,
                    mLatLng?.longitude,
                    searchText,
                    isGrabMapSelected,
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

        fun searchPlaceIndexForText(searchText: String?) {
            _searchLocationList.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searchPlaceIndexForText(
                    mLatLng?.latitude,
                    mLatLng?.longitude,
                    searchText,
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
                                TravelMode.Walking.value,
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
                                TravelMode.Truck.value,
                            )
                        }
                    three.await()
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
                                TravelMode.Car.value,
                            )
                        }
                    one.await()
                }
            }
        }

        fun calculateGrabDistance(
            latitude: Double?,
            longitude: Double?,
            latDestination: Double?,
            lngDestination: Double?,
            isAvoidFerries: Boolean?,
            isAvoidTolls: Boolean?,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val two =
                    async {
                        calculateDistanceFromMode(
                            latitude,
                            longitude,
                            latDestination,
                            lngDestination,
                            isAvoidFerries,
                            isAvoidTolls,
                            TravelMode.Walking.value,
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
                            TRAVEL_MODE_BICYCLE,
                        )
                    }
                three.await()
                val one =
                    async {
                        calculateDistanceFromMode(
                            latitude,
                            longitude,
                            latDestination,
                            lngDestination,
                            isAvoidFerries,
                            isAvoidTolls,
                            TRAVEL_MODE_MOTORCYCLE,
                        )
                    }
                one.await()
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
                    override fun distanceSuccess(success: CalculateRouteResponse) {
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
                        override fun distanceSuccess(success: CalculateRouteResponse) {
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
            data: CalculateRouteResponse,
        ) {
            _navigationData.trySend(HandleResult.Loading)
            data.legs[0].let { legs ->
                mNavigationListModel.clear()
                viewModelScope.launch(Dispatchers.IO) {
                    val address =
                        async {
                            legs.steps.let { steps ->
                                steps.forEach { step ->
                                    getAddressFromLatLng(step.startPosition[1], step.startPosition[0], step)
                                }
                            }
                        }
                    address.await()
                    mNavigationResponse = NavigationResponse()
                    legs.let { leg ->
                        mNavigationResponse?.duration = Units.getTime(context, leg.durationSeconds)
                        mNavigationResponse?.distance = leg.distance
                        mNavigationResponse?.startLat = leg.startPosition[1]
                        mNavigationResponse?.startLng = leg.startPosition[0]
                        mNavigationResponse?.endLat = leg.endPosition[1]
                        mNavigationResponse?.endLng = leg.endPosition[0]
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

        suspend fun getAddressFromLatLng(
            latitude: Double?,
            longitude: Double?,
            step: Step,
            isTimeDialog: Boolean = false,
        ) {
            _navigationTimeDialogData.trySend(HandleResult.Loading)
            getLocationSearchUseCase.searchNavigationPlaceIndexForPosition(
                latitude,
                longitude,
                step,
                object : NavigationDataInterface {
                    override fun getNavigationList(navigationData: NavigationData) {
                        if (isTimeDialog) {
                            _navigationTimeDialogData.trySend(
                                HandleResult.Success(
                                    navigationData,
                                ),
                            )
                        } else {
                            mNavigationListModel.add(navigationData)
                        }
                    }

                    override fun internetConnectionError(error: String) {
                        _navigationTimeDialogData.trySend(
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

        fun getAddressLineFromLatLng(
            longitude: Double?,
            latitude: Double?,
        ) {
            _navigationTimeDialogData.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                getLocationSearchUseCase.searPlaceIndexForPosition(
                    latitude,
                    longitude,
                    object : SearchDataInterface {
                        override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResponse) {
                            _addressLineData.trySend(
                                HandleResult.Success(
                                    SearchResponse(
                                        searchPlaceIndexForPositionResult,
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

        fun setMapListData(
            context: Context,
            isGrabMapEnable: Boolean = false,
        ) {
            val items =
                arrayListOf(
                    MapStyleInnerData(
                        context.getString(R.string.map_light),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        false,
                        R.drawable.light,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_streets),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        false,
                        R.drawable.streets,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_navigation),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        false,
                        R.drawable.navigation,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_dark_gray),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_DARK),
                        listOf(TYPE_VECTOR),
                        false,
                        R.drawable.dark_gray,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_light_gray),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        false,
                        R.drawable.light_gray,
                    ),
                    MapStyleInnerData(
                        context.getString(R.string.map_imagery),
                        context.getString(R.string.map_esri),
                        listOf(ATTRIBUTE_SATELLITE),
                        listOf(TYPE_RASTER),
                        false,
                        R.drawable.imagery,
                    ),
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_explore),
                        context.resources.getString(R.string.here),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        image = R.mipmap.ic_here_explore,
                        isSelected = false,
                        mMapName = MapNames.HERE_EXPLORE,
                        mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE,
                    ),
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_contrast),
                        context.resources.getString(R.string.here),
                        listOf(ATTRIBUTE_DARK, ATTRIBUTE_3D),
                        listOf(TYPE_VECTOR),
                        image = R.mipmap.ic_here_contrast,
                        isSelected = false,
                        mMapName = MapNames.HERE_CONTRAST,
                        mMapStyleName = MapStyles.VECTOR_HERE_CONTRAST,
                    ),
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_explore_truck),
                        context.resources.getString(R.string.here),
                        listOf(ATTRIBUTE_LIGHT, ATTRIBUTE_TRUCK),
                        listOf(TYPE_VECTOR),
                        image = R.mipmap.ic_here_explore_truck,
                        isSelected = false,
                        mMapName = MapNames.HERE_EXPLORE_TRUCK,
                        mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE_TRUCK,
                    ),
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_hybrid),
                        context.resources.getString(R.string.here),
                        listOf(ATTRIBUTE_SATELLITE),
                        listOf(TYPE_VECTOR, TYPE_RASTER),
                        image = R.mipmap.ic_here_hybrid,
                        isSelected = false,
                        mMapName = MapNames.HERE_HYBRID,
                        mMapStyleName = MapStyles.HYBRID_HERE_EXPLORE_SATELLITE,
                    ),
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_raster),
                        context.resources.getString(R.string.here),
                        listOf(ATTRIBUTE_SATELLITE),
                        listOf(TYPE_RASTER),
                        image = R.mipmap.ic_here_imagery,
                        isSelected = false,
                        mMapName = MapNames.HERE_IMAGERY,
                        mMapStyleName = MapStyles.RASTER_HERE_EXPLORE_SATELLITE,
                    ),
                )
            if (isGrabMapEnable) {
                items.add(
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_grab_light),
                        context.resources.getString(R.string.grab),
                        listOf(ATTRIBUTE_LIGHT),
                        listOf(TYPE_VECTOR),
                        image = R.drawable.grab_light,
                        isSelected = false,
                        mMapName = MapNames.GRAB_LIGHT,
                        mMapStyleName = MapStyles.GRAB_LIGHT,
                    ),
                )
                items.add(
                    MapStyleInnerData(
                        context.resources.getString(R.string.map_grab_dark),
                        context.resources.getString(R.string.grab),
                        listOf(ATTRIBUTE_DARK),
                        listOf(TYPE_VECTOR),
                        image = R.drawable.grab_dark,
                        isSelected = false,
                        mMapName = MapNames.GRAB_DARK,
                        mMapStyleName = MapStyles.GRAB_DARK,
                    ),
                )
            }
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_standard_light),
                    context.resources.getString(R.string.open_data),
                    listOf(ATTRIBUTE_LIGHT),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.standard_light,
                    isSelected = false,
                    mMapName = MapNames.OPEN_DATA_STANDARD_LIGHT,
                    mMapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_LIGHT,
                ),
            )
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_standard_dark),
                    context.resources.getString(R.string.open_data),
                    listOf(ATTRIBUTE_DARK),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.standard_dark,
                    isSelected = false,
                    mMapName = MapNames.OPEN_DATA_STANDARD_DARK,
                    mMapStyleName = MapStyles.VECTOR_OPEN_DATA_STANDARD_DARK,
                ),
            )
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_visualization_light),
                    context.resources.getString(R.string.open_data),
                    listOf(ATTRIBUTE_LIGHT),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.visualization_light,
                    isSelected = false,
                    mMapName = MapNames.OPEN_DATA_VISUALIZATION_LIGHT,
                    mMapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_LIGHT,
                ),
            )
            items.add(
                MapStyleInnerData(
                    context.resources.getString(R.string.map_visualization_dark),
                    context.resources.getString(R.string.open_data),
                    listOf(ATTRIBUTE_DARK),
                    listOf(TYPE_VECTOR),
                    image = R.drawable.visualization_dark,
                    isSelected = false,
                    mMapName = MapNames.OPEN_DATA_VISUALIZATION_DARK,
                    mMapStyleName = MapStyles.VECTOR_OPEN_DATA_VISUALIZATION_DARK,
                ),
            )
            mStyleList.clear()

            mStyleList =
                items
                    .groupBy { it.provider }
                    .map { (providerName, providerItems) ->
                        MapStyleData(
                            styleNameDisplay = providerName,
                            isSelected = false, // Set isSelected as per your requirement
                            mapInnerData = providerItems,
                        )
                    } as ArrayList<MapStyleData>

            mStyleListForFilter.clear()
            mStyleListForFilter.addAll(mStyleList)
            providerOptions =
                items
                    .map { it.provider }
                    .distinct()
                    .map { FilterOption(it) } as ArrayList<FilterOption>

            attributeOptions =
                items
                    .flatMap { it.attributes }
                    .distinct()
                    .map { FilterOption(it) } as ArrayList<FilterOption>

            typeOptions =
                items
                    .flatMap { it.types }
                    .distinct()
                    .map { FilterOption(it) } as ArrayList<FilterOption>
        }

        fun filterAndSortItems(
            context: Context,
            searchQuery: String? = null,
            providerNames: List<String>? = null,
            attributes: List<String>? = null,
            types: List<String>? = null,
        ): List<MapStyleData> {
            val providerOrder =
                listOf(
                    context.resources.getString(R.string.map_esri),
                    context.resources.getString(R.string.here),
                    context.resources.getString(R.string.grab),
                    context.resources.getString(R.string.open_data),
                )

            // Convert the providers to a sequence for more efficient processing
            return mStyleListForFilter
                .asSequence()
                .filter { providerNames?.contains(it.styleNameDisplay) ?: true }
                .mapNotNull { provider ->
                    val filteredItems =
                        provider.mapInnerData
                            ?.asSequence()
                            ?.filter { item ->
                                val matchesSearchQuery =
                                    searchQuery?.let { sq ->
                                        var attributeDataContains = false
                                        var typeDataContains = false
                                        item.attributes.forEach {
                                            if (!attributeDataContains) {
                                                attributeDataContains = it.contains(sq, ignoreCase = true)
                                            }
                                        }
                                        item.types.forEach {
                                            if (!typeDataContains) {
                                                typeDataContains = it.contains(sq, ignoreCase = true)
                                            }
                                        }
                                        item.mapName?.contains(sq, ignoreCase = true) == true ||
                                            item.provider.contains(
                                                sq,
                                                ignoreCase = true,
                                            ) ||
                                            attributeDataContains ||
                                            typeDataContains
                                    } ?: true

                                val hasRequiredAttributes =
                                    attributes?.let { attrs ->
                                        item.attributes.intersect(attrs).isNotEmpty()
                                    } ?: true

                                val hasRequiredTypes =
                                    types?.let { ts ->
                                        item.types.intersect(ts).isNotEmpty()
                                    } ?: true

                                matchesSearchQuery && hasRequiredAttributes && hasRequiredTypes
                            }?.toList()

                    if (filteredItems?.isEmpty() == true) {
                        null
                    } else {
                        provider.copy(mapInnerData = filteredItems)
                    }
                }.sortedBy { providerOrder.indexOf(it.styleNameDisplay) }
                .toList() // Convert the result back to a list
        }
    }
