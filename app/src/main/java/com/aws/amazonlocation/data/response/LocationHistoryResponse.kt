package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.location.model.GetDevicePositionHistoryResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class LocationHistoryResponse(var errorMessage: String?, var response: GetDevicePositionHistoryResponse?)
