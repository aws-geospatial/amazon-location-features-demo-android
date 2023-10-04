package com.aws.amazonlocation.ui.main.signin

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignInResult
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.data.response.SignOutData
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.utils.ExcludeFromJacocoGeneratedReport
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_PROVIDER
import com.aws.amazonlocation.utils.KEY_USER_DETAILS
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.isRunningTest
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class SignInViewModel @Inject constructor(
    private var mGetAuthUseCase: AuthUseCase,
    private var mPreferenceManager: PreferenceManager,
) :
    ViewModel() {

    private val _signInResponse =
        Channel<HandleResult<String>>(Channel.BUFFERED)
    val mSignInResponse: Flow<HandleResult<String>> =
        _signInResponse.receiveAsFlow()

    private val _signOutResponse =
        Channel<HandleResult<SignOutData>>(Channel.BUFFERED)
    val mSignOutResponse: Flow<HandleResult<SignOutData>> =
        _signOutResponse.receiveAsFlow()

    fun signInWithAmazon(
        activity: Activity,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isRunningTest) {
                signInWhileRunningTest(activity)
            } else {
                mGetAuthUseCase.signInWithAmazon(
                    activity,
                    object : SignInInterface {
                        override fun getUserDetails(mLoginResponse: LoginResponse) {
                            mPreferenceManager.setValue(
                                KEY_USER_DETAILS,
                                Gson().toJson(mLoginResponse),
                            )
                            mLoginResponse.idToken?.let { idToken ->
                                mPreferenceManager.setValue(
                                    KEY_ID_TOKEN,
                                    idToken,
                                )
                            }

                            mLoginResponse.provider?.let { provider ->
                                mPreferenceManager.setValue(
                                    KEY_PROVIDER,
                                    provider,
                                )
                            }
                            mPreferenceManager.setValue(
                                KEY_USER_DETAILS,
                                Gson().toJson(mLoginResponse),
                            )
                            _signInResponse.trySend(HandleResult.Success(mLoginResponse.success!!))
                        }

                        override fun signInFailed(exception: String?) {
                            exception?.let {
                                _signInResponse.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it,
                                        ),
                                    ),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    fun signOutWithAmazon(context: Context, isDisconnectFromAWSRequired: Boolean) {
        viewModelScope.launch {
            if (isRunningTest) {
                signOutWhileRunningTest(context, isDisconnectFromAWSRequired)
            } else {
                mGetAuthUseCase.signOutWithAmazon(
                    context,
                    isDisconnectFromAWSRequired,
                    object : SignInInterface {
                        override fun signOutSuccess(
                            success: String,
                            isDisconnectFromAWSRequired: Boolean,
                        ) {
                            _signOutResponse.trySend(
                                HandleResult.Success(
                                    SignOutData(
                                        success,
                                        isDisconnectFromAWSRequired,
                                    ),
                                ),
                            )
                        }

                        override fun signOutFailed(error: String) {
                            _signOutResponse.trySend(
                                HandleResult.Error(
                                    DataSourceException.Error(
                                        error,
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
    @ExcludeFromJacocoGeneratedReport
    private fun signInWhileRunningTest(activity: Activity) {
        AWSMobileClient.getInstance().signIn(
            BuildConfig.USER_LOGIN_NAME,
            BuildConfig.USER_LOGIN_PASSWORD,
            null,
            object : Callback<SignInResult> {
                override fun onResult(result: SignInResult?) {
                    val mLoginResponse = LoginResponse()
                    mLoginResponse.success =
                        activity.resources.getString(R.string.login_success)
                    mLoginResponse.name = AWSMobileClient.getInstance().username
                    val userState = AWSMobileClient.getInstance().currentUserState()
                    if (userState.details.containsKey("token")) {
                        mLoginResponse.idToken = userState.details["token"]
                    }

                    if (userState.details.containsKey("provider")) {
                        mLoginResponse.provider = userState.details["provider"]
                    }

                    mPreferenceManager.setValue(
                        KEY_USER_DETAILS,
                        Gson().toJson(mLoginResponse),
                    )
                    mLoginResponse.idToken?.let { idToken ->
                        mPreferenceManager.setValue(
                            KEY_ID_TOKEN,
                            idToken,
                        )
                    }

                    mLoginResponse.provider?.let { provider ->
                        mPreferenceManager.setValue(
                            KEY_PROVIDER,
                            provider,
                        )
                    }
                    mPreferenceManager.setValue(
                        KEY_USER_DETAILS,
                        Gson().toJson(mLoginResponse),
                    )
                    _signInResponse.trySend(HandleResult.Success(mLoginResponse.success!!))
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }
            },
        )
    }
    @ExcludeFromJacocoGeneratedReport
    private fun signOutWhileRunningTest(
        context: Context,
        isDisconnectFromAWSRequired: Boolean
    ) {
        AWSMobileClient.getInstance().signOut()
        _signOutResponse.trySend(
            HandleResult.Success(
                SignOutData(
                    context.resources.getString(R.string.sign_out_successfully),
                    isDisconnectFromAWSRequired,
                ),
            ),
        )
    }
}
