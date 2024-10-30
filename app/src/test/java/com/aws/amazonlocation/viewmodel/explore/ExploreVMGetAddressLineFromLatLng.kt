package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
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
class ExploreVMGetAddressLineFromLatLng : BaseTest() {

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
    fun getAddressLineFromLatLngSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searPlaceIndexForPosition(anyOrNull(), anyOrNull(), any()))
            .thenAnswer {
                val callback: SearchDataInterface = it.arguments[2] as SearchDataInterface
                Responses.RESPONSE_ADDRESS_LINE_FROM_LAT_LNG.reverseGeocodeResponse?.let { it1 ->
                    callback.getAddressData(
                        it1
                    )
                }
            }

        mExploreVM.addressLineData.test {
            mExploreVM.getAddressLineFromLatLng(GATE_WAY_OF_INDIA_LAT_LNG.longitude, GATE_WAY_OF_INDIA_LAT_LNG.latitude)
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)

            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, data.reverseGeocodeResponse == Responses.RESPONSE_ADDRESS_LINE_FROM_LAT_LNG.reverseGeocodeResponse)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAddressLineFromLatLngError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searPlaceIndexForPosition(anyOrNull(), anyOrNull(), any()))
            .thenAnswer {
                val callback: SearchDataInterface = it.arguments[2] as SearchDataInterface
                callback.error("")
            }

        mExploreVM.addressLineData.test {
            mExploreVM.getAddressLineFromLatLng(null, null)
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_DATA_NOT_EMPTY, data.reverseGeocodeResponse == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAddressLineFromLatLngInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searPlaceIndexForPosition(anyOrNull(), anyOrNull(), any()))
            .thenAnswer {
                val callback: SearchDataInterface = it.arguments[2] as SearchDataInterface
                callback.internetConnectionError(NO_INTERNET_ERROR)
            }

        mExploreVM.addressLineData.test {
            mExploreVM.getAddressLineFromLatLng(null, null)
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)

            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            val error = (result as HandleResult.Error).exception.messageResource
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, error == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
