package com.aws.amazonlocation.viewmodel.tracking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_LOADING
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.utils.PreferenceManager
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SignInViewModelTest : BaseTest() {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var authImp: AuthImp

    private lateinit var authUseCase: AuthUseCase

    private lateinit var signInViewModel: SignInViewModel
    private lateinit var preferenceManager: PreferenceManager

    override fun setUp() {
        super.setUp()
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        authImp = AuthImp(mRemoteDataSourceImpl)
        authUseCase = AuthUseCase(authImp)
        signInViewModel = SignInViewModel(authUseCase, preferenceManager)
    }

    @Test
    fun fetchTokensWithOkHttpSuccess() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.fetchTokensWithOkHttp(any(), any())).thenAnswer {
                val callback: SignInInterface = it.arguments[1] as SignInInterface
                val responseBody =
                    """
                    {
                        "access_token": "sampleAccessToken123",
                        "id_token": "sampleIdToken123",
                        "refresh_token": "sampleRefreshToken123",
                        "expires_in": 3600
                    }
                    """.trimIndent()

                val response =
                    Response
                        .Builder()
                        .code(200)
                        .message("OK")
                        .protocol(Protocol.HTTP_1_1)
                        .request(
                            Request
                                .Builder()
                                .url("https://yourdomain.com/oauth2/token")
                                .build(),
                        ).body(
                            responseBody.toResponseBody("application/json".toMediaTypeOrNull()),
                        ).build()
                callback.fetchTokensWithOkHttpSuccess("success", response)
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.fetchTokensWithOkHttp("test")
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Success).response == "success",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }


    @Test
    fun fetchTokensWithOkHttpHttpFail() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.fetchTokensWithOkHttp(any(), any())).thenAnswer {
                val callback: SignInInterface = it.arguments[1] as SignInInterface

                val response =
                    Response
                        .Builder()
                        .code(400)
                        .message("error")
                        .protocol(Protocol.HTTP_1_1)
                        .request(
                            Request
                                .Builder()
                                .url("https://yourdomain.com/oauth2/token")
                                .build(),
                        ).build()
                callback.fetchTokensWithOkHttpSuccess("error", response)
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.fetchTokensWithOkHttp("test")
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Error).exception.messageResource == "fail",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun fetchTokensWithOkHttpFail() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.fetchTokensWithOkHttp(any(), any())).thenAnswer {
                val callback: SignInInterface = it.arguments[1] as SignInInterface
                callback.fetchTokensWithOkHttpFailed("fail")
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.fetchTokensWithOkHttp("test")
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Error).exception.messageResource == "fail",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }


    @Test
    fun refreshTokensWithOkHttpSuccess() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.refreshTokensWithOkHttp(any())).thenAnswer {
                val callback: SignInInterface = it.arguments[0] as SignInInterface
                val responseBody =
                    """
                    {
                        "access_token": "sampleAccessToken123",
                        "id_token": "sampleIdToken123",
                        "expires_in": 3600
                    }
                    """.trimIndent()

                val response =
                    Response
                        .Builder()
                        .code(200)
                        .message("OK")
                        .protocol(Protocol.HTTP_1_1)
                        .request(
                            Request
                                .Builder()
                                .url("https://yourdomain.com/oauth2/token")
                                .build(),
                        ).body(
                            responseBody.toResponseBody("application/json".toMediaTypeOrNull()),
                        ).build()
                callback.refreshTokensWithOkHttpSuccess("success", response)
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.refreshTokensWithOkHttp()
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Success).response == "success",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun refreshTokensWithOkHttpHttpFail() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.refreshTokensWithOkHttp( any())).thenAnswer {
                val callback: SignInInterface = it.arguments[0] as SignInInterface

                val response =
                    Response
                        .Builder()
                        .code(400)
                        .message("error")
                        .protocol(Protocol.HTTP_1_1)
                        .request(
                            Request
                                .Builder()
                                .url("https://yourdomain.com/oauth2/token")
                                .build(),
                        ).build()
                callback.refreshTokensWithOkHttpSuccess("error", response)
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.refreshTokensWithOkHttp()
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Error).exception.messageResource == "fail",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun refreshTokensWithOkHttpFail() =
        runTest {
            Mockito.`when`(mRemoteDataSourceImpl.refreshTokensWithOkHttp(any())).thenAnswer {
                val callback: SignInInterface = it.arguments[0] as SignInInterface
                callback.refreshTokensWithOkHttpFailed("fail")
            }

            signInViewModel.fetchTokenResponse.test {
                signInViewModel.refreshTokensWithOkHttp()
                var result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
                result = awaitItem()
                Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
                Assert.assertTrue(
                    TEST_FAILED_DUE_TO_INCORRECT_DATA,
                    (result as HandleResult.Error).exception.messageResource == "fail",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
}
