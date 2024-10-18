package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.location.model.BatchDeleteDevicePositionHistoryResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class DeleteLocationHistoryResponse(
    var errorMessage: String?,
    var response: BatchDeleteDevicePositionHistoryResponse?
)
