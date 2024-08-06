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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchPlaceSuggestionSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchPlaceSuggestions(anyOrNull(), anyOrNull(), any(), any(), any())).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.getSearchPlaceSuggestionResponse(Responses.RESPONSE_SEARCH_TEXT_RIO_TINTO)
        }

        mExploreVM.mLatLng = DEFAULT_LOCATION

        mExploreVM.searchForSuggestionsResultList.test {
            mExploreVM.searchPlaceSuggestion(SEARCH_TEXT_RIO_TINTO, true)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.data.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchPlaceSuggestionError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.searchPlaceSuggestions(anyOrNull(), anyOrNull(), any(), any(), any())).thenAnswer {
            val callback: SearchPlaceInterface = it.arguments[3] as SearchPlaceInterface
            callback.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }

        mExploreVM.searchForSuggestionsResultList.test {
            mExploreVM.searchPlaceSuggestion(SEARCH_TEXT_RIO_TINTO, false)
            var result = awaitItem()
            assert(result is HandleResult.Loading)
            result = awaitItem()
            assert(result is HandleResult.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun testMLatLngInitializedToNull() {
        mExploreVM.mLatLng = null
        assertEquals(null, mExploreVM.mLatLng)
    }

    @Test
    fun testMStartLatLngInitializedToNull() {
        mExploreVM.mStartLatLng = null
        assertEquals(null, mExploreVM.mStartLatLng)
    }

    @Test
    fun testMDestinationLatLngInitializedToNull() {
        mExploreVM.mDestinationLatLng = null
        assertEquals(null, mExploreVM.mDestinationLatLng)
    }
    @Test
    fun testMSearchDirectionOriginDataToNull() {
        mExploreVM.mSearchDirectionOriginData = null
        assertEquals(null, mExploreVM.mSearchDirectionOriginData)
    }
    @Test
    fun testmCarDataToNull() {
        mExploreVM.mCarData = null
        assertEquals(null, mExploreVM.mCarData)
    }
    @Test
    fun testmWalkingDataToNull() {
        mExploreVM.mWalkingData = null
        assertEquals(null, mExploreVM.mWalkingData)
    }
    @Test
    fun testmTruckDataToNull() {
        mExploreVM.mTruckData = null
        assertEquals(null, mExploreVM.mTruckData)
    }
    @Test
    fun testmBicycleDataToNull() {
        mExploreVM.mBicycleData = null
        assertEquals(null, mExploreVM.mBicycleData)
    }
    @Test
    fun testmMotorcycleDataToNull() {
        mExploreVM.mMotorcycleData = null
        assertEquals(null, mExploreVM.mMotorcycleData)
    }

    @Test
    fun testMStyleListInitializedToEmpty() {
        mExploreVM.mStyleList = arrayListOf()
        assertEquals(0, mExploreVM.mStyleList.size)
    }

    @Test
    fun testProviderOptionsInitializedToEmpty() {
        mExploreVM.providerOptions = arrayListOf()
        assertEquals(0, mExploreVM.providerOptions.size)
    }

    @Test
    fun testAttributeOptionsInitializedToEmpty() {
        mExploreVM.attributeOptions = arrayListOf()
        assertEquals(0, mExploreVM.attributeOptions.size)
    }

    @Test
    fun testTypeOptionsInitializedToEmpty() {
        mExploreVM.typeOptions = arrayListOf()
        mExploreVM.mStyleListForFilter = arrayListOf()
        mExploreVM.mIsPlaceSuggestion = false
        assertEquals(0, mExploreVM.typeOptions.size)
    }
}
