package com.aws.amazonlocation.viewmodel.tracking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.getDateRange
import com.aws.amazonlocation.mock.DEVICE_ID
import com.aws.amazonlocation.mock.MOCK_ERROR
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_LOADING
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
import com.aws.amazonlocation.mock.TRACKING_HISTORY_END_DATE
import com.aws.amazonlocation.mock.TRACKING_HISTORY_START_DATE
import com.aws.amazonlocation.ui.main.tracking.TrackingViewModel
import com.aws.amazonlocation.utils.TrackerCons
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrackingVMGetLocationHistory : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var geofenceImp: GeofenceImp

    private lateinit var geofenceUseCase: GeofenceUseCase

    private lateinit var mTrackingViewModel: TrackingViewModel

    override fun setUp() {
        super.setUp()

        geofenceImp = GeofenceImp(mRemoteDataSourceImpl)
        geofenceUseCase = GeofenceUseCase(geofenceImp)
        mTrackingViewModel = TrackingViewModel(geofenceUseCase)
    }

    @Test
    fun getLocationHistorySuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getLocationHistory(any(), any(), any(), any(), any())).thenAnswer {
            val callback: LocationHistoryInterface = it.arguments[4] as LocationHistoryInterface
            val data = Responses.RESPONSE_LOCATION_HISTORY
            data.response = data.response
            data.errorMessage = data.errorMessage
            callback.success(data)
        }

        val dates = getDateRange(TRACKING_HISTORY_START_DATE, TRACKING_HISTORY_END_DATE)

        mTrackingViewModel.mGetLocationHistoryList.test {
            mTrackingViewModel.getLocationHistory(TrackerCons.TRACKER_COLLECTION, DEVICE_ID, dates.first, dates.second)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as HandleResult.Success).response == Responses.RESPONSE_LOCATION_HISTORY.response)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLocationHistoryError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getLocationHistory(any(), any(), any(), any(), any()))
            .thenAnswer {
                val callback: LocationHistoryInterface = it.arguments[4] as LocationHistoryInterface
                callback.success(Responses.RESPONSE_ERROR_LOCATION_HISTORY_NO_ERROR)
            }
            .thenAnswer {
                val callback: LocationHistoryInterface = it.arguments[4] as LocationHistoryInterface
                callback.success(Responses.RESPONSE_ERROR_LOCATION_HISTORY)
            }

        val dates = getDateRange(TRACKING_HISTORY_START_DATE, TRACKING_HISTORY_END_DATE)

        mTrackingViewModel.mGetLocationHistoryList.test {
            mTrackingViewModel.getLocationHistory(TrackerCons.TRACKER_COLLECTION, DEVICE_ID, dates.first, dates.second)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            mTrackingViewModel.getLocationHistory(TrackerCons.TRACKER_COLLECTION, DEVICE_ID, dates.first, dates.second)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as HandleResult.Error).exception.messageResource == MOCK_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
