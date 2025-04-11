package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchResponse(
    var reverseGeocodeResponse: ReverseGeocodeResponse? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)
