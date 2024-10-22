package com.aws.amazonlocation.domain.`interface`

import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SearchDataInterface {
    fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResponse)

    fun internetConnectionError(error: String) {}
    fun error(error: String) {}
}
