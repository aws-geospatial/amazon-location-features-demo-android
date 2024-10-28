package com.aws.amazonlocation.viewmodel.geofence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.mock.DEFAULT_LOCATION
import com.aws.amazonlocation.mock.NO_DATA_FOUND
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.SEARCH_TEXT_RIO_TINTO
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceVMSearchTest : BaseTest() {

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
    fun searchPlaceSuccess() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.searchPlaceIndexForText(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any()
            )
        ).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[4] as SearchPlaceInterface
            callback.success(Responses.RESPONSE_SEARCH_TEXT_RIO_TINTO)
        }

        mGeofenceViewModel.mGeofenceSearchLocationList.test {
            mGeofenceViewModel.geofenceSearchPlaceIndexForText(
                SEARCH_TEXT_RIO_TINTO,
                DEFAULT_LOCATION
            )
            val result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.data.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchPlaceFail() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.searchPlaceIndexForText(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any()
            )
        ).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[4] as SearchPlaceInterface
            callback.error(SearchSuggestionResponse(error = NO_DATA_FOUND))
        }

        mGeofenceViewModel.mGeofenceSearchLocationList.test {
            mGeofenceViewModel.geofenceSearchPlaceIndexForText(
                SEARCH_TEXT_RIO_TINTO,
                DEFAULT_LOCATION
            )
            val result = awaitItem()
            assert(result is HandleResult.Error)
            assert((result as HandleResult.Error).exception.messageResource.toString().isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
