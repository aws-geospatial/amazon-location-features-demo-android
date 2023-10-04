package com.aws.amazonlocation.viewmodel.signin

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.amazonaws.mobile.client.AWSMobileClient
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.utils.PreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment


@RunWith(RobolectricTestRunner::class)
class SignInVMSignIn : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var locationSearchImp: AuthImp

    private lateinit var locationSearchUseCase: AuthUseCase

    private lateinit var mSignInVM: SignInViewModel

    override fun setUp() {
        super.setUp()
        val mPreferenceManager = PreferenceManager(context)
        locationSearchImp = AuthImp(mRemoteDataSourceImpl)
        locationSearchUseCase = AuthUseCase(locationSearchImp)
        mSignInVM = SignInViewModel(locationSearchUseCase, mPreferenceManager)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signInSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signInWithAmazon(anyOrNull(), any())).thenAnswer {
            val mLoginResponse = LoginResponse()
            mLoginResponse.success = SIGN_IN_SUCCESSFULLY
            mLoginResponse.name = AWSMobileClient.getInstance().username
            mLoginResponse.idToken = "test"
            mLoginResponse.provider = "test-provider"
            val callback: SignInInterface = it.arguments[1] as SignInInterface
            callback.getUserDetails(mLoginResponse)
        }

        mSignInVM.mSignInResponse.test {
            val dummyActivity = MainActivity()
            mSignInVM.signInWithAmazon(activity = dummyActivity)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                (result as? HandleResult.Success)?.response == SIGN_IN_SUCCESSFULLY
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signInFailed() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signInWithAmazon(anyOrNull(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[1] as SignInInterface
            callback.signInFailed(SIGN_IN_FAILED)
        }

        mSignInVM.mSignInResponse.test {
            val dummyActivity = MainActivity()
            mSignInVM.signInWithAmazon(activity = dummyActivity)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                (result as? HandleResult.Error)?.exception?.messageResource == SIGN_IN_FAILED
            )
        }
    }
}
