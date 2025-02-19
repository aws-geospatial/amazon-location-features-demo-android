package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.georoutes.model.RouteContinueHighwayStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteContinueStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteEnterHighwayStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteExitStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteKeepStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteRampStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteRoundaboutEnterStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteRoundaboutExitStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteRoundaboutPassStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteTurnStepDetails
import aws.sdk.kotlin.services.georoutes.model.RouteUTurnStepDetails

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class NavigationResponse(

    var duration: String? = null,
    var distance: Double? = null,
    var destinationAddress: String? = null,
    var time: String? = null,
    var navigationList: ArrayList<NavigationData> = ArrayList()
)

class NavigationData(
    var duration: Double? = null,
    var distance: Double? = null,
    var startLat: Double? = null,
    var startLng: Double? = null,
    var endLat: Double? = null,
    var endLng: Double? = null,
    var destinationAddress: String? = null,
    var region: String? = null,
    var subRegion: String? = null,
    var country: String? = null,
    var type: String? = null,
    var routeTurnStepDetails: RouteTurnStepDetails? = null,
    var routeContinueHighwayStepDetails: RouteContinueHighwayStepDetails? = null,
    var routeContinueStepDetails: RouteContinueStepDetails? = null,
    var routeEnterHighwayStepDetails: RouteEnterHighwayStepDetails? = null,
    var routeExitStepDetails: RouteExitStepDetails? = null,
    var routeKeepStepDetails: RouteKeepStepDetails? = null,
    var routeRampStepDetails: RouteRampStepDetails? = null,
    var routeRoundaboutEnterStepDetails: RouteRoundaboutEnterStepDetails? = null,
    var routeRoundaboutExitStepDetails: RouteRoundaboutExitStepDetails? = null,
    var routeRoundaboutPassStepDetails: RouteRoundaboutPassStepDetails? = null,
    var routeUTurnStepDetails: RouteUTurnStepDetails? = null,
    var isDataSuccess: Boolean = false
) {
    fun getAddress(): String {
        var mAddress = ""
        if (isDataSuccess) {
            mAddress += if (!destinationAddress.isNullOrEmpty()) {
                destinationAddress
            } else if (!subRegion.isNullOrEmpty() && !region.isNullOrEmpty()) {
                "$subRegion, $region"
            } else if (!region.isNullOrEmpty() && !country.isNullOrEmpty()) {
                "$region, $country"
            } else if (!country.isNullOrEmpty()) {
                country
            } else {
                ""
            }
        } else {
            mAddress = destinationAddress.toString()
        }
        return mAddress
    }
}
