package com.aws.amazonlocation.viewmodel.geofence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.NO_DATA_FOUND
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import com.aws.amazonlocation.utils.GeofenceCons
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceVMGeofenceListTest : BaseTest() {

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
    fun geofenceListSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(anyOrNull(), anyOrNull())).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
            val data = Responses.RESPONSE_GEOFENCE_LIST
            data.message = data.message
            data.geofenceList = data.geofenceList
            callback.getGeofenceList(Responses.RESPONSE_GEOFENCE_LIST)
        }

        mGeofenceViewModel.mGetGeofenceList.test {
            mGeofenceViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun geofenceListError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getGeofenceList(anyOrNull(), anyOrNull())).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[1] as GeofenceAPIInterface
            callback.getGeofenceList(Responses.ERROR_RESPONSE_GEOFENCE_LIST)
        }

        mGeofenceViewModel.mGetGeofenceList.test {
            mGeofenceViewModel.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Error)
            (result as HandleResult.Error).exception.messageResource?.equals(NO_DATA_FOUND)
                ?.let { assert(it) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
