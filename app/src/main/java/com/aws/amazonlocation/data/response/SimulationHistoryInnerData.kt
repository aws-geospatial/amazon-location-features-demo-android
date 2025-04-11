package com.aws.amazonlocation.data.response

import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class SimulationHistoryInnerData(
    var latitude: Double,
    var longitude: Double,
    var receivedTime: Date
)
