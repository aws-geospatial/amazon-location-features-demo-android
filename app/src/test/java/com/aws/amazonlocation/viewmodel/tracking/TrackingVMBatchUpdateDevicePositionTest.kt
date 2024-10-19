package com.aws.amazonlocation.viewmodel.tracking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.DEVICE_ID
import com.aws.amazonlocation.mock.GATE_WAY_OF_INDIA_LAT_LNG
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
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
class TrackingVMBatchUpdateDevicePositionTest : BaseTest() {

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
    fun batchUpdateDevicePositionSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.batchUpdateDevicePosition(any(), any(), any(), any())).thenAnswer {
            val callback: BatchLocationUpdateInterface = it.arguments[3] as BatchLocationUpdateInterface
            callback.success(UpdateBatchLocationResponse(null, true))
        }

        val position = listOf(GATE_WAY_OF_INDIA_LAT_LNG.longitude, GATE_WAY_OF_INDIA_LAT_LNG.latitude)

        mTrackingViewModel.mGetUpdateDevicePosition.test {
            mTrackingViewModel.batchUpdateDevicePosition(TrackerCons.TRACKER_COLLECTION, position, DEVICE_ID)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
