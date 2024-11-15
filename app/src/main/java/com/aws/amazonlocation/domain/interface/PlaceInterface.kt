package com.aws.amazonlocation.domain.`interface`

import aws.sdk.kotlin.services.geoplaces.model.GetPlaceResponse
import com.aws.amazonlocation.data.common.DataSourceException

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface PlaceInterface {

    fun placeSuccess(success: GetPlaceResponse) {}

    fun placeFailed(exception: DataSourceException) {}

    fun internetConnectionError(exception: String) {}
}
