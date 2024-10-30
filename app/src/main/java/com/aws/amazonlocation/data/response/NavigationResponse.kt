package com.aws.amazonlocation.data.response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class NavigationResponse(

    var duration: String? = null,
    var distance: Double? = null,
    var destinationAddress: String? = null,
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
