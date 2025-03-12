package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.geoplaces.model.Address
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_LOADING
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ExploreVMCalculateNavigationLine : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

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
    fun calculateNavigationLineSuccess() = runTest {
        val param = Responses.RESPONSE_CALCULATE_DISTANCE_CAR

        val searchSuggestionData = getSearchSuggestionData()

        mExploreVM.mSearchSuggestionData = searchSuggestionData
        mExploreVM.mSearchDirectionOriginData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData?.isDestination = true

        mExploreVM.mNavigationData.test {
            mExploreVM.calculateNavigationLine(context, param)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE, data.navigationList.size == 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateNavigationLineWalkSuccess() = runTest {
        val param = Responses.RESPONSE_CALCULATE_DISTANCE_WALKING

        val searchSuggestionData = getSearchSuggestionData()

        mExploreVM.mSearchSuggestionData = searchSuggestionData
        mExploreVM.mSearchDirectionOriginData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData?.isDestination = true

        mExploreVM.mNavigationData.test {
            mExploreVM.calculateNavigationLine(context, param)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE, data.navigationList.size == 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateNavigationLineFerriesSuccess() = runTest {
        val param = Responses.RESPONSE_CALCULATE_DISTANCE_FERRIES

        val searchSuggestionData = getSearchSuggestionData()

        mExploreVM.mSearchSuggestionData = searchSuggestionData
        mExploreVM.mSearchDirectionOriginData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData = searchSuggestionData
        mExploreVM.mSearchDirectionDestinationData?.isDestination = true

        mExploreVM.mNavigationData.test {
            mExploreVM.calculateNavigationLine(context, param)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            val data = (result as HandleResult.Success).response
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE, data.navigationList.size == 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun getSearchSuggestionData(): SearchSuggestionData {
        val it = Responses.SEARCH_RESPONSE_TAJ
        val searchSuggestionData = SearchSuggestionData()
        it.reverseGeocodeResponse?.let { searchPlaceIndexForPositionResult ->
            searchSuggestionData.text =
                searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.label
            searchSuggestionData.searchText =
                searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.label
            searchSuggestionData.distance =
                searchPlaceIndexForPositionResult.resultItems?.get(0)?.distance?.toDouble()
            searchSuggestionData.isDestination = true
            searchSuggestionData.placeId =
                searchPlaceIndexForPositionResult.resultItems?.get(0)?.placeId
            it.latitude?.let { lat ->
                it.longitude?.let { lng ->
                    searchSuggestionData.position = listOf(lng, lat)
                }
            }
            searchSuggestionData.isPlaceIndexForPosition = false
            val address = Address {
                label = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.label
                addressNumber = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.addressNumber
                street = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.street
                country = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.country
                region = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.region
                subRegion = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.subRegion
                postalCode = searchPlaceIndexForPositionResult.resultItems?.get(0)?.address?.postalCode
            }
            searchSuggestionData.amazonLocationAddress = address
        }
        return searchSuggestionData
    }
}
