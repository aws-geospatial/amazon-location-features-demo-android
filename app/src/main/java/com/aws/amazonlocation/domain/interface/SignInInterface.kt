package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.LoginResponse
import okhttp3.Response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SignInInterface {

    fun fetchTokensWithOkHttpFailed(exception: String?) {}

    fun fetchTokensWithOkHttpSuccess(success: String, response: Response) {}

    fun refreshTokensWithOkHttpFailed(exception: String?) {}

    fun refreshTokensWithOkHttpSuccess(success: String, response: Response) {}
}
