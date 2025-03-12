package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.DEFAULT_LOCATION
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.SEARCH_TEXT_RIO_TINTO
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ExploreVMSearchSuggestionTest : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mContext: Context = RuntimeEnvironment.getApplication().applicationContext

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
    fun searchPlaceSuggestionSuccess() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.searchPlaceSuggestions(anyOrNull(), anyOrNull(), any(), any())
        ).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.getSearchPlaceSuggestionResponse(Responses.RESPONSE_SEARCH_TEXT_RIO_TINTO)
        }

        mExploreVM.mLatLng = DEFAULT_LOCATION

        mExploreVM.searchForSuggestionsResultList.test {
            mExploreVM.searchPlaceSuggestion(SEARCH_TEXT_RIO_TINTO)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.data.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchPlaceSuggestionError() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.searchPlaceSuggestions(anyOrNull(), anyOrNull(), any(), any())
        ).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.internetConnectionError(
                mContext.resources.getString(R.string.check_your_internet_connection_and_try_again)
            )
        }

        mExploreVM.searchForSuggestionsResultList.test {
            mExploreVM.searchPlaceSuggestion(SEARCH_TEXT_RIO_TINTO)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
