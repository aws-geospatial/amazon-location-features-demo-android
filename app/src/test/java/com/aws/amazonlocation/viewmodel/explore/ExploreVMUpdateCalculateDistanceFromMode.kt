package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.georoutes.model.RouteTravelMode
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
class ExploreVMUpdateCalculateDistanceFromMode : BaseTest() {

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
    fun updateCalculateDistanceFromModeSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val mode = it.arguments[6] as String
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface

            when (mode) {
                RouteTravelMode.Pedestrian.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
                RouteTravelMode.Car.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
                RouteTravelMode.Truck.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
            }
        }

        mExploreVM.mUpdateCalculateDistance.test {
            val start = DISTANCE_COORDINATE_FROM
            val end = DISTANCE_COORDINATE_TO
            mExploreVM.updateCalculateDistanceFromMode(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Car.value)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
            mExploreVM.updateCalculateDistanceFromMode(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Pedestrian.value)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
            mExploreVM.updateCalculateDistanceFromMode(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Truck.value)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCalculateDistanceFromModeError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val distanceType = it.arguments[6] as String?
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.distanceFailed(DataSourceException.Error(distanceType!!))
        }

        mExploreVM.mUpdateCalculateDistance.test {
            mExploreVM.updateCalculateDistanceFromMode(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Car.value)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Car.value)

            mExploreVM.updateCalculateDistanceFromMode(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Pedestrian.value)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Pedestrian.value)

            mExploreVM.updateCalculateDistanceFromMode(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Truck.value)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Truck.value)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCalculateDistanceFromModeInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.internetConnectionError(NO_INTERNET_ERROR)
        }

        mExploreVM.mUpdateCalculateDistance.test {
            mExploreVM.updateCalculateDistanceFromMode(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, RouteTravelMode.Car.value)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as? HandleResult.Error)?.exception?.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
