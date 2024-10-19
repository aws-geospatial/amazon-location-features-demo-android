package com.aws.amazonlocation.ui.main.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.services.location.model.BatchDeleteDevicePositionHistoryResponse
import aws.sdk.kotlin.services.location.model.GetDevicePositionHistoryResponse
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private var mGetGeofenceUseCase: GeofenceUseCase
) :
    ViewModel() {

    private val _getGeofenceList =
        Channel<HandleResult<ArrayList<ListGeofenceResponseEntry>>>(Channel.BUFFERED)
    val mGetGeofenceList: Flow<HandleResult<ArrayList<ListGeofenceResponseEntry>>> =
        _getGeofenceList.receiveAsFlow()

    private val _getLocationHistoryList =
        Channel<HandleResult<GetDevicePositionHistoryResponse>>(Channel.BUFFERED)
    val mGetLocationHistoryList: Flow<HandleResult<GetDevicePositionHistoryResponse>> =
        _getLocationHistoryList.receiveAsFlow()

    private val _getLocationHistoryTodayList =
        Channel<HandleResult<GetDevicePositionHistoryResponse>>(Channel.BUFFERED)
    val mGetLocationHistoryTodayList: Flow<HandleResult<GetDevicePositionHistoryResponse>> =
        _getLocationHistoryTodayList.receiveAsFlow()

    private val _getUpdateDevicePosition =
        Channel<HandleResult<UpdateBatchLocationResponse>>(Channel.BUFFERED)
    val mGetUpdateDevicePosition: Flow<HandleResult<UpdateBatchLocationResponse>> =
        _getUpdateDevicePosition.receiveAsFlow()

    private val _deleteLocationHistoryList =
        Channel<HandleResult<BatchDeleteDevicePositionHistoryResponse>>(Channel.BUFFERED)
    val mDeleteLocationHistoryList: Flow<HandleResult<BatchDeleteDevicePositionHistoryResponse>> =
        _deleteLocationHistoryList.receiveAsFlow()

    fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.batchUpdateDevicePosition(
                trackerName,
                position,
                deviceId,
                object : BatchLocationUpdateInterface {
                    override fun success(searchResponse: UpdateBatchLocationResponse) {
                        if (searchResponse.isLocationDataAdded) {
                            _getUpdateDevicePosition.trySend(HandleResult.Success(searchResponse))
                        }
                    }
                }
            )
        }
    }

    fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date
    ) {
        _getLocationHistoryList.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.getLocationHistory(
                trackerName,
                deviceId,
                dateStart,
                dateEnd,
                object : LocationHistoryInterface {
                    override fun success(historyResponse: LocationHistoryResponse) {
                        if (historyResponse.response == null) {
                            historyResponse.errorMessage?.let {
                                _getLocationHistoryList.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it
                                        )
                                    )
                                )
                            }
                        } else {
                            historyResponse.response?.let {
                                _getLocationHistoryList.trySend(HandleResult.Success(it))
                            }
                        }
                    }
                }
            )
        }
    }

    fun getLocationHistoryToday(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.getLocationHistory(
                trackerName,
                deviceId,
                dateStart,
                dateEnd,
                object : LocationHistoryInterface {
                    override fun success(historyResponse: LocationHistoryResponse) {
                        if (historyResponse.response == null) {
                            historyResponse.errorMessage?.let {
                                _getLocationHistoryTodayList.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it
                                        )
                                    )
                                )
                            }
                        } else {
                            historyResponse.response?.let {
                                _getLocationHistoryTodayList.trySend(HandleResult.Success(it))
                            }
                        }
                    }
                }
            )
        }
    }

    fun deleteLocationHistory(
        trackerName: String,
        deviceId: String
    ) {
        _deleteLocationHistoryList.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.deleteLocationHistory(
                trackerName,
                deviceId,
                object : LocationDeleteHistoryInterface {
                    override fun success(historyResponse: DeleteLocationHistoryResponse) {
                        if (historyResponse.response == null) {
                            historyResponse.errorMessage?.let {
                                _deleteLocationHistoryList.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it
                                        )
                                    )
                                )
                            }
                        } else {
                            historyResponse.response?.let {
                                _deleteLocationHistoryList.trySend(HandleResult.Success(it))
                            }
                        }
                    }
                }
            )
        }
    }

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
}
