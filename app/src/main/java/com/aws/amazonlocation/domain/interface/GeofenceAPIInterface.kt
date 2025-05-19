package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.GeofenceData

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface GeofenceAPIInterface {
    fun getGeofenceList(geofenceData: GeofenceData) {}
}
