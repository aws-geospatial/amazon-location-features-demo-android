package com.aws.amazonlocation.viewmodel.simulation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.SimulationUseCase
import com.aws.amazonlocation.mock.MOCK_ERROR
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_LOADING
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
import com.aws.amazonlocation.ui.main.simulation.SimulationViewModel
import com.aws.amazonlocation.utils.GeofenceCons
import com.aws.amazonlocation.utils.simulationCollectionName
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
class SimulationVMGetGeofenceList : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var geofenceImp: GeofenceImp

    private lateinit var geofenceUseCase: SimulationUseCase

    private lateinit var simulationViewModel: SimulationViewModel

    override fun setUp() {
        super.setUp()

        geofenceImp = GeofenceImp(mRemoteDataSourceImpl)
        geofenceUseCase = SimulationUseCase(geofenceImp)
        simulationViewModel = SimulationViewModel(geofenceUseCase)
    }

    @Test
    fun getSimulationListSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(any(), any())).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
            callback.getGeofenceList(Responses.RESPONSE_TRACKER_GEOFENCE_LIST)
        }

        simulationViewModel.mGetGeofenceList.test {
            simulationViewModel.getGeofenceList(simulationCollectionName[0])
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSimulationListError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(any(), any()))
            .thenAnswer {
                val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
                callback.getGeofenceList(Responses.RESPONSE_ERROR_TRACKER_GEOFENCE_LIST_NO_ERROR)
            }
            .thenAnswer {
                val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
                callback.getGeofenceList(Responses.RESPONSE_ERROR_TRACKER_GEOFENCE_LIST)
            }

        simulationViewModel.mGetGeofenceList.test {
            simulationViewModel.getGeofenceList(simulationCollectionName[0])
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            simulationViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE,
                (result as HandleResult.Error).exception.messageResource == MOCK_ERROR
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
