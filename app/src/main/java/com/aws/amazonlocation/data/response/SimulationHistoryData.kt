package com.aws.amazonlocation.data.response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class SimulationHistoryData(
    var headerData: String,
    var isBusStopData: Boolean,
    var busStopCount: Int,
    var devicePositionData: SimulationHistoryInnerData? = null
)
