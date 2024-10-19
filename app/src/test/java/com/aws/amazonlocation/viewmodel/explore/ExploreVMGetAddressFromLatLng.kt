package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
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

@RunWith(RobolectricTestRunner::class)
class ExploreVMGetAddressFromLatLng : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var locationSearchImp: LocationSearchImp

    private lateinit var locationSearchUseCase: LocationSearchUseCase

    private lateinit var mExploreVM: ExploreViewModel

    override fun setUp() {
        super.setUp()

        locationSearchImp = LocationSearchImp(mRemoteDataSourceImpl)
        locationSearchUseCase = LocationSearchUseCase(locationSearchImp)
        mExploreVM = ExploreViewModel(locationSearchUseCase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAddressFromLatLngSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchNavigationPlaceIndexForPosition(anyOrNull(), anyOrNull(), any(), any()))
            .thenAnswer {
                val callback: NavigationDataInterface = it.arguments[3] as NavigationDataInterface
                callback.getNavigationList(Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_1)
            }

        val param = Responses.RESPONSE_CALCULATE_DISTANCE_CAR
        val startPosition = param.legs.first().startPosition
        val step = param.legs.first().steps.first()

        mExploreVM.mNavigationTimeDialogData.test {
            mExploreVM.getAddressFromLatLng(startPosition[0], startPosition[1], step, true)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, data == Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAddressFromLatLngInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchNavigationPlaceIndexForPosition(anyOrNull(), anyOrNull(), any(), any()))
            .thenAnswer {
                val callback: NavigationDataInterface = it.arguments[3] as NavigationDataInterface
                callback.internetConnectionError(NO_INTERNET_ERROR)
            }

        val param = Responses.RESPONSE_CALCULATE_DISTANCE_CAR
        val startPosition = param.legs.first().startPosition
        val step = param.legs.first().steps.first()

        mExploreVM.mNavigationTimeDialogData.test {
            mExploreVM.getAddressFromLatLng(startPosition[0], startPosition[1], step, true)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as HandleResult.Error).exception.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
