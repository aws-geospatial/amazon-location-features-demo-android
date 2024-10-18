package com.aws.amazonlocation.ui.main.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.utils.KEY_ACCESS_TOKEN
import com.aws.amazonlocation.utils.KEY_AUTH_EXPIRES_IN
import com.aws.amazonlocation.utils.KEY_AUTH_FETCH_TIME
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_REFRESH_TOKEN
import com.aws.amazonlocation.utils.KEY_RESPONSE_ACCESS_TOKEN
import com.aws.amazonlocation.utils.KEY_RESPONSE_EXPIRES_IN
import com.aws.amazonlocation.utils.KEY_RESPONSE_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_RESPONSE_REFRESH_TOKEN
import com.aws.amazonlocation.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONObject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class SignInViewModel
    @Inject
    constructor(
        private var mGetAuthUseCase: AuthUseCase,
        private var mPreferenceManager: PreferenceManager,
    ) : ViewModel() {
        private val _fetchTokenResponse =
            Channel<HandleResult<String>>(Channel.BUFFERED)
        val fetchTokenResponse: Flow<HandleResult<String>> =
            _fetchTokenResponse.receiveAsFlow()

        fun fetchTokensWithOkHttp(authorizationCode: String) {
            _fetchTokenResponse.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                mGetAuthUseCase.fetchTokensWithOkHttp(
                    authorizationCode,
                    object : SignInInterface {
                        override fun fetchTokensWithOkHttpFailed(exception: String?) {
                            _fetchTokenResponse.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        exception.toString(),
                                    ),
                                ),
                            )
                        }

                        override fun fetchTokensWithOkHttpSuccess(
                            success: String,
                            response: Response,
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body.string()
                                val jsonResponse = JSONObject(responseBody)
                                val accessToken = jsonResponse.getString(KEY_RESPONSE_ACCESS_TOKEN)
                                val idToken = jsonResponse.getString(KEY_RESPONSE_ID_TOKEN)
                                val refreshToken = jsonResponse.getString(KEY_RESPONSE_REFRESH_TOKEN)
                                val expiresIn = jsonResponse.getInt(KEY_RESPONSE_EXPIRES_IN)
                                mPreferenceManager.setValue(KEY_ACCESS_TOKEN, accessToken)
                                mPreferenceManager.setValue(KEY_ID_TOKEN, idToken)
                                mPreferenceManager.setValue(KEY_REFRESH_TOKEN, refreshToken)
                                mPreferenceManager.setValue(KEY_AUTH_EXPIRES_IN, expiresIn.toLong())
                                mPreferenceManager.setValue(
                                    KEY_AUTH_FETCH_TIME,
                                    System.currentTimeMillis(),
                                )
                                _fetchTokenResponse.trySend(
                                    HandleResult.Success(
                                        "success",
                                    ),
                                )
                            } else {
                                _fetchTokenResponse.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            "fail",
                                        ),
                                    ),
                                )
                            }
                        }
                    },
                )
            }
        }

        fun refreshTokensWithOkHttp() {
            _fetchTokenResponse.trySend(HandleResult.Loading)
            viewModelScope.launch(Dispatchers.IO) {
                mGetAuthUseCase.refreshTokensWithOkHttp(
                    object : SignInInterface {
                        override fun refreshTokensWithOkHttpSuccess(
                            success: String,
                            response: Response,
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body.string()
                                val jsonResponse = JSONObject(responseBody)
                                val accessToken = jsonResponse.getString(KEY_RESPONSE_ACCESS_TOKEN)
                                val idToken = jsonResponse.getString(KEY_RESPONSE_ID_TOKEN)
                                val expiresIn = jsonResponse.getInt(KEY_RESPONSE_EXPIRES_IN)
                                mPreferenceManager.setValue(KEY_ACCESS_TOKEN, accessToken)
                                mPreferenceManager.setValue(KEY_ID_TOKEN, idToken)
                                mPreferenceManager.setValue(KEY_AUTH_EXPIRES_IN, expiresIn.toLong())
                                mPreferenceManager.setValue(
                                    KEY_AUTH_FETCH_TIME,
                                    System.currentTimeMillis(),
                                )
                                _fetchTokenResponse.trySend(
                                    HandleResult.Success(
                                        "success",
                                    ),
                                )
                            } else {
                                _fetchTokenResponse.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            "fail",
                                        ),
                                    ),
                                )
                            }
                        }

                        override fun refreshTokensWithOkHttpFailed(exception: String?) {
                            _fetchTokenResponse.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        exception.toString(),
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
