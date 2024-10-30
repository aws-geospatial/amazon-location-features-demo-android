package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.geoplaces.model.Address


// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

data class SearchSuggestionResponse(
    var text: String? = null,
    var maxResults: Int? = null,
    var data: ArrayList<SearchSuggestionData> = ArrayList(),
    var error: String ? = null,
)

data class SearchSuggestionData(
    var placeId: String? = null,
    var searchText: String? = null,
    var text: String? = null,
    var distance: Double? = null,
    var isDestination: Boolean? = false,
    var isPlaceIndexForPosition: Boolean = false,
    var amazonLocationAddress: Address? = null,
    var position: List<Double> ? = null,
    var queryId: String ? = null
)
