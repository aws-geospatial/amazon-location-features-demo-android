package com.aws.amazonlocation.domain.usecase

import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.repository.SimulationRepository
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationUseCase @Inject constructor(private val mGeofenceRepository: SimulationRepository) {

    suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceInterface: GeofenceAPIInterface
    ) =
        mGeofenceRepository.getGeofenceList(collectionName, mGeofenceInterface)

    suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        deviceId: String,
        identityId: String,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    ) = mGeofenceRepository.evaluateGeofence(
        trackerName,
        position1,
        deviceId,
        identityId,
        batchLocationUpdateInterface
    )
}
