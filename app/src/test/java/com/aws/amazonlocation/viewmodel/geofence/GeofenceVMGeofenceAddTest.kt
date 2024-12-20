package com.aws.amazonlocation.viewmodel.geofence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.DEFAULT_LOCATION
import com.aws.amazonlocation.mock.NO_DATA_FOUND
import com.aws.amazonlocation.mock.TEST_DATA_9
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import com.aws.amazonlocation.utils.GeofenceCons
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceVMGeofenceAddTest : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var geofenceImp: GeofenceImp

    private lateinit var geofenceUseCase: GeofenceUseCase

    private lateinit var mGeofenceViewModel: GeofenceViewModel

    override fun setUp() {
        super.setUp()

        geofenceImp = GeofenceImp(mRemoteDataSourceImpl)
        geofenceUseCase = GeofenceUseCase(geofenceImp)
        mGeofenceViewModel = GeofenceViewModel(geofenceUseCase)
    }

    @Test
    fun geofenceAddSuccess() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.addGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[4] as GeofenceAPIInterface
            callback.addGeofence(
                AddGeofenceResponse(
                    isGeofenceDataAdded = true,
                    errorMessage = null
                )
            )
        }

        mGeofenceViewModel.mAddGeofence.test {
            mGeofenceViewModel.addGeofence(
                TEST_DATA_9,
                GeofenceCons.GEOFENCE_COLLECTION,
                80.toDouble(),
                DEFAULT_LOCATION
            )
            val result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.isGeofenceDataAdded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun geofenceAddFail() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.addGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[4] as GeofenceAPIInterface
            callback.addGeofence(
                AddGeofenceResponse(
                    isGeofenceDataAdded = false,
                    errorMessage = NO_DATA_FOUND
                )
            )
        }

        mGeofenceViewModel.mAddGeofence.test {
            mGeofenceViewModel.addGeofence(
                TEST_DATA_9,
                GeofenceCons.GEOFENCE_COLLECTION,
                80.toDouble(),
                DEFAULT_LOCATION
            )
            val result = awaitItem()
            assert(result is HandleResult.Error)
            (result as HandleResult.Error).exception.messageResource?.equals(NO_DATA_FOUND)
                ?.let { assert(it) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
