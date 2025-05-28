package com.aws.amazonlocation.ui.main.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.SimulationUseCase
import com.aws.amazonlocation.utils.ExcludeFromJacocoGeneratedReport
import com.aws.amazonlocation.utils.simulationCollectionName
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class SimulationViewModel @Inject constructor(
    private var mGetGeofenceUseCase: SimulationUseCase
) :
    ViewModel() {

    private val _getGeofenceList =
        Channel<HandleResult<SimulationGeofenceData>>(Channel.BUFFERED)
    val mGetGeofenceList: Flow<HandleResult<SimulationGeofenceData>> =
        _getGeofenceList.receiveAsFlow()

    private val _getUpdateDevicePosition =
        Channel<HandleResult<UpdateBatchLocationResponse>>(Channel.BUFFERED)
    val mGetUpdateDevicePosition: Flow<HandleResult<UpdateBatchLocationResponse>> =
        _getUpdateDevicePosition.receiveAsFlow()

    @ExcludeFromJacocoGeneratedReport
    fun callAllSimulation() {
        viewModelScope.launch(Dispatchers.IO) {
            simulationCollectionName.forEach {
                val data = async {
                    getGeofenceList(it)
                }
                data.await()
            }
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
                            _getGeofenceList.trySend(
                                HandleResult.Success(
                                    SimulationGeofenceData(
                                        collectionName,
                                        geofenceData.geofenceList
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun evaluateGeofence(
        trackerName: String,
        position: List<Double>? = null,
        deviceId: String,
        identityId: String
    ) {
        _getUpdateDevicePosition.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.evaluateGeofence(
                trackerName,
                position,
                deviceId,
                identityId,
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
}
