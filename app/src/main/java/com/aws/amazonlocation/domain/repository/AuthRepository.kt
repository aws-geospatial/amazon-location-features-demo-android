package com.aws.amazonlocation.domain.repository

import com.aws.amazonlocation.domain.`interface`.SignInInterface

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface AuthRepository {

    suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface)

    suspend fun refreshTokensWithOkHttp(
        signInInterface: SignInInterface
    )
}
