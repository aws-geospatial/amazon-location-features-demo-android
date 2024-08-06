package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
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
class ExploreVMSearchPlaceIndexTest : BaseTest() {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchPlaceIndexForTextSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchPlaceIndexForText(anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.success(Responses.RESPONSE_PLACE_INDEX_RIO_TINTO)
        }

        mExploreVM.mLatLng = DEFAULT_LOCATION

        mExploreVM.mSearchLocationList.test {
            mExploreVM.searchPlaceIndexForText(SEARCH_TEXT_RIO_TINTO)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchPlaceIndexForTextError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchPlaceIndexForText(anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.error(Responses.RESPONSE_PLACE_INDEX_RIO_TINTO_ERROR)
        }.thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            val data = SearchSuggestionResponse()
            data.data = arrayListOf()
            callback.error(data)
        }

        mExploreVM.mLatLng = null

        mExploreVM.mSearchLocationList.test {
            mExploreVM.searchPlaceIndexForText(SEARCH_TEXT_RIO_TINTO)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            mExploreVM.searchPlaceIndexForText(SEARCH_TEXT_RIO_TINTO)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchPlaceIndexForTextInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchPlaceIndexForText(anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.internetConnectionError(NO_INTERNET_ERROR)
        }

        mExploreVM.mSearchLocationList.test {
            mExploreVM.searchPlaceIndexForText(SEARCH_TEXT_RIO_TINTO)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as HandleResult.Error).exception.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
