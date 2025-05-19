package com.aws.amazonlocation.domain.repository

import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SimulationRepository {

    suspend fun getGeofenceList(collectionName: String, mGeofenceAPIInterface: GeofenceAPIInterface)

    suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        deviceId: String,
        identityId: String,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    )
}
