package com.aws.amazonlocation.data.repository

import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.repository.AuthRepository

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AuthImp(private val mRemoteDataSourceImpl: RemoteDataSourceImpl) : AuthRepository {

    override suspend fun fetchTokensWithOkHttp(authorizationCode: String, signInInterface: SignInInterface) {
        mRemoteDataSourceImpl.fetchTokensWithOkHttp(authorizationCode, signInInterface)
    }

    override suspend fun refreshTokensWithOkHttp(
        signInInterface: SignInInterface
    ) {
        mRemoteDataSourceImpl.refreshTokensWithOkHttp(
            signInInterface
        )
    }
}
