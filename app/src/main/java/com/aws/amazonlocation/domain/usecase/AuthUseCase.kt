package com.aws.amazonlocation.domain.usecase

import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.repository.AuthRepository
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AuthUseCase @Inject constructor(private val mAuthRepository: AuthRepository) {

    suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface) =
        mAuthRepository.fetchTokensWithOkHttp(authorizationCode, signInInterface)

    suspend fun refreshTokensWithOkHttp(
        signInInterface: SignInInterface
    ) =
        mAuthRepository.refreshTokensWithOkHttp(signInInterface)
}
