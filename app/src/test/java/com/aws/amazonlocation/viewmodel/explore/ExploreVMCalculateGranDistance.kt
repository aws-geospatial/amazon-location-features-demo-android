package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.location.model.TravelMode
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.AVOID_FERRIES
import com.aws.amazonlocation.mock.AVOID_TOLLS
import com.aws.amazonlocation.mock.DISTANCE_COORDINATE_FROM
import com.aws.amazonlocation.mock.DISTANCE_COORDINATE_TO
import com.aws.amazonlocation.mock.NO_INTERNET_ERROR
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_LOADING
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import com.aws.amazonlocation.utils.TRAVEL_MODE_BICYCLE
import com.aws.amazonlocation.utils.TRAVEL_MODE_MOTORCYCLE
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
class ExploreVMCalculateGranDistance : BaseTest() {

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

    @Test
    fun calculateGrabDistanceSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val mode = it.arguments[6] as String
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface

            when (mode) {
                TravelMode.Walking.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
                TRAVEL_MODE_BICYCLE -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_BICYCLE)
                TRAVEL_MODE_MOTORCYCLE -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_MOTORCYCLE)
            }
        }

        mExploreVM.mCalculateDistance.test {
            val start = DISTANCE_COORDINATE_FROM
            val end = DISTANCE_COORDINATE_TO
            mExploreVM.calculateGrabDistance(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_BICYCLE)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_MOTORCYCLE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateGrabDistanceError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val distanceType = it.arguments[6] as String?
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.distanceFailed(DataSourceException.Error(distanceType!!))
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateGrabDistance(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TravelMode.Walking.value)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TRAVEL_MODE_BICYCLE)

            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TRAVEL_MODE_MOTORCYCLE)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateGrabDistanceInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.internetConnectionError(NO_INTERNET_ERROR)
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateGrabDistance(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as? HandleResult.Error)?.exception?.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
