package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import org.maplibre.android.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class CalculateDistanceResponse(
    var name: String? = null,
    var calculateRouteResult: CalculateRoutesResponse? = null,
    var sourceLatLng: LatLng? = null,
    var destinationLatLng: LatLng? = null
)
