package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class GeofenceData(
    var geofenceList: ArrayList<ListGeofenceResponseEntry> = ArrayList(),
    var message: String?
)
