package com.aws.amazonlocation.data.response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class UpdateBatchLocationResponse(
    var errorMessage: String?,
    var isLocationDataAdded: Boolean = false
)
