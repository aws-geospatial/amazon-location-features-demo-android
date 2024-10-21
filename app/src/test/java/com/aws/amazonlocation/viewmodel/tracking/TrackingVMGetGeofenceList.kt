package com.aws.amazonlocation.viewmodel.tracking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.tracking.TrackingViewModel
import com.aws.amazonlocation.utils.GeofenceCons
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class TrackingVMGetGeofenceList : BaseTest() {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getGeofenceListSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(any(), any())).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
            callback.getGeofenceList(Responses.RESPONSE_TRACKER_GEOFENCE_LIST)
        }

        mTrackingViewModel.mGetGeofenceList.test {
            mTrackingViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as HandleResult.Success).response == Responses.RESPONSE_TRACKER_GEOFENCE_LIST.geofenceList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getGeofenceListError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(any(), any()))
            .thenAnswer {
                val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
                callback.getGeofenceList(Responses.RESPONSE_ERROR_TRACKER_GEOFENCE_LIST_NO_ERROR)
            }
            .thenAnswer {
                val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
                callback.getGeofenceList(Responses.RESPONSE_ERROR_TRACKER_GEOFENCE_LIST)
            }

        mTrackingViewModel.mGetGeofenceList.test {
            mTrackingViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as HandleResult.Success).response.isEmpty())
            mTrackingViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as HandleResult.Error).exception.messageResource == MOCK_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
