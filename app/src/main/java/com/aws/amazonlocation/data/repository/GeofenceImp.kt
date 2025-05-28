package com.aws.amazonlocation.data.repository

import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.repository.SimulationRepository

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceImp(private val mRemoteDataSource: RemoteDataSourceImpl) :
    SimulationRepository {

    override suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        mRemoteDataSource.getGeofenceList(collectionName, mGeofenceAPIInterface)
    }

    override suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>?,
        deviceId: String,
        identityId: String,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    ) {
        mRemoteDataSource.evaluateGeofence(
            trackerName,
            position1,
            deviceId,
            identityId,
            batchLocationUpdateInterface
        )
    }
}
