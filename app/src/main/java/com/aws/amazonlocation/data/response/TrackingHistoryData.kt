package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.location.model.DevicePosition

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class TrackingHistoryData(
    var headerId: String,
    var headerString: String,
    var headerData: String,
    var devicePositionData: DevicePosition? = null
)
