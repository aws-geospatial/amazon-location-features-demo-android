package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchResponse(
    var searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResponse? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)

