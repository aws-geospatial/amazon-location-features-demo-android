package com.aws.amazonlocation.ui.main.geofence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class GeofenceViewModel @Inject constructor(
    private var mGetGeofenceUseCase: GeofenceUseCase
) :
    ViewModel() {

    private val _getGeofenceList =
        Channel<HandleResult<ArrayList<ListGeofenceResponseEntry>>>(Channel.BUFFERED)
    val mGetGeofenceList: Flow<HandleResult<ArrayList<ListGeofenceResponseEntry>>> =
        _getGeofenceList.receiveAsFlow()

    private val _addGeofence =
        Channel<HandleResult<AddGeofenceResponse>>(Channel.BUFFERED)
    val mAddGeofence: Flow<HandleResult<AddGeofenceResponse>> =
        _addGeofence.receiveAsFlow()

    private val _deleteGeofence =
        Channel<HandleResult<DeleteGeofence>>(Channel.BUFFERED)
    val mDeleteGeofence: Flow<HandleResult<DeleteGeofence>> =
        _deleteGeofence.receiveAsFlow()

    private val _geofenceSearchForSuggestionsResultList =
        Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
    val mGeofenceSearchForSuggestionsResultList: Flow<HandleResult<SearchSuggestionResponse>> =
        _geofenceSearchForSuggestionsResultList.receiveAsFlow()

    private val _geofenceSearchLocationList =
        Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
    val mGeofenceSearchLocationList: Flow<HandleResult<SearchSuggestionResponse>> =
        _geofenceSearchLocationList.receiveAsFlow()

    fun getGeofenceList(collectionName: String) {
        _getGeofenceList.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.getGeofenceList(
                collectionName,
                object : GeofenceAPIInterface {
                    override fun getGeofenceList(geofenceData: GeofenceData) {
                        if (!geofenceData.message.isNullOrEmpty()) {
                            geofenceData.message?.let {
                                _getGeofenceList.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it
                                        )
                                    )
                                )
                            }
                        } else {
                            _getGeofenceList.trySend(HandleResult.Success(geofenceData.geofenceList))
                        }
                    }
                }
            )
        }
    }

    fun addGeofence(geofenceId: String, collectionName: String, radius: Double?, latLng: LatLng?) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.addGeofence(
                geofenceId,
                collectionName,
                radius,
                latLng,
                object : GeofenceAPIInterface {
                    override fun addGeofence(response: AddGeofenceResponse) {
                        if (response.isGeofenceDataAdded) {
                            _addGeofence.trySend(HandleResult.Success(response))
                        } else {
                            response.errorMessage?.let { error ->
                                _addGeofence.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            error
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.deleteGeofence(
                position,
                data,
                object : GeofenceAPIInterface {
                    override fun deleteGeofence(deleteGeofence: DeleteGeofence) {
                        if (!deleteGeofence.errorMessage.isNullOrEmpty()) {
                            _deleteGeofence.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        deleteGeofence.errorMessage!!
                                    )
                                )
                            )
                        } else if (deleteGeofence.data != null) {
                            this@GeofenceViewModel._deleteGeofence.trySend(
                                HandleResult.Success(
                                    deleteGeofence
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun geofenceSearchPlaceSuggestion(
        searchText: String,
        latLng: LatLng?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.searchPlaceSuggestionList(
                latLng?.latitude,
                latLng?.longitude,
                searchText,
                object : SearchPlaceInterface {
                    override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                        _geofenceSearchForSuggestionsResultList.trySend(
                            HandleResult.Success(
                                suggestionResponse!!
                            )
                        )
                    }
                }
            )
        }
    }

    fun geofenceSearchPlaceIndexForText(
        searchText: String?,
        latLng: LatLng?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.searchPlaceIndexForText(
                latLng?.latitude,
                latLng?.longitude,
                searchText,
                object : SearchPlaceInterface {
                    override fun success(searchResponse: SearchSuggestionResponse) {
                        _geofenceSearchLocationList.trySend(HandleResult.Success(searchResponse))
                    }

                    override fun error(searchResponse: SearchSuggestionResponse) {
                        searchResponse.error?.let {
                            DataSourceException.Error(
                                it
                            )
                        }?.let {
                            HandleResult.Error(
                                it
                            )
                        }?.let { _geofenceSearchLocationList.trySend(it) }
                    }
                }
            )
        }
    }
}
