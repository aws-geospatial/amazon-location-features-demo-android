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
class SignInVMSignOut : BaseTest() {

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
    fun signOutSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signOutWithAmazon(anyOrNull(),anyOrNull(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[2] as SignInInterface
            callback.signOutSuccess(SIGN_OUT_SUCCESSFULLY, false)
        }

        mSignInVM.mSignOutResponse.test {
            mSignInVM.signOutWithAmazon(context = context, false)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                (result as? HandleResult.Success)?.response?.message == SIGN_OUT_SUCCESSFULLY
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signOutFailed() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signOutWithAmazon(anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[2] as SignInInterface
            callback.signOutFailed(SIGN_OUT_FAILED)
        }

        mSignInVM.mSignOutResponse.test {
            mSignInVM.signOutWithAmazon(context = context, false)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                (result as? HandleResult.Error)?.exception?.messageResource == SIGN_OUT_FAILED
            )
        }
    }
}
