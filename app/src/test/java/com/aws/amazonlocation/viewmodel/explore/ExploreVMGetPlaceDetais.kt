package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.PlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.*
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
class ExploreVMGetPlaceDetais : BaseTest() {

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
    fun getPlaceSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getPlace(anyOrNull(), any()))
            .thenAnswer {
                val callback: PlaceInterface = it.arguments[1] as PlaceInterface
                Responses.GET_PLACE_RESPONSE.let { it1 ->
                    callback.placeSuccess(
                        it1
                    )
                }
            }

        mExploreVM.placeData.test {
            mExploreVM.getPlaceData("test")
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)

            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, data.contacts?.phones?.get(0)?.value == "6666444422")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPlaceError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getPlace(anyOrNull(), any()))
            .thenAnswer {
                val callback: PlaceInterface = it.arguments[1] as PlaceInterface
                callback.placeFailed(DataSourceException.Error(""))
            }

        mExploreVM.placeData.test {
            mExploreVM.getPlaceData("test")
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Error)
            val data = (result as HandleResult.Error).exception
            Assert.assertTrue(TEST_FAILED_DUE_TO_DATA_NOT_EMPTY, data.messageResource == "")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getPlaceInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.getPlace(anyOrNull(), any()))
            .thenAnswer {
                val callback: PlaceInterface = it.arguments[1] as PlaceInterface
                callback.internetConnectionError(NO_INTERNET_ERROR)
            }

        mExploreVM.placeData.test {
            mExploreVM.getPlaceData("test")
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, awaitItem() is HandleResult.Loading)

            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            val error = (result as HandleResult.Error).exception.messageResource
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, error == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
