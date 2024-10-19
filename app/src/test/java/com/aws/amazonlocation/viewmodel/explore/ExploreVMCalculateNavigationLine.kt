package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.location.model.Place
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
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
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
        Mockito.`when`(mRemoteDataSourceImpl.searchNavigationPlaceIndexForPosition(anyOrNull(), anyOrNull(), any(), any()))
            .thenAnswer {
                val callback: NavigationDataInterface = it.arguments[3] as NavigationDataInterface
                callback.getNavigationList(Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_1)
            }
            .thenAnswer {
                val callback: NavigationDataInterface = it.arguments[3] as NavigationDataInterface
                callback.getNavigationList(Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_2)
            }

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
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                data.navigationList[0] == Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_1,
            )
            Assert.assertTrue(
                TEST_FAILED_DUE_TO_INCORRECT_DATA,
                data.navigationList[1] == Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_2,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun getSearchSuggestionData(): SearchSuggestionData {
        val it = Responses.SEARCH_RESPONSE_TAJ
        val searchSuggestionData = SearchSuggestionData()
        it.searchPlaceIndexForPositionResult?.let { searchPlaceIndexForPositionResult ->
            searchSuggestionData.text =
                searchPlaceIndexForPositionResult.results[0].place?.label
            searchSuggestionData.searchText =
                searchPlaceIndexForPositionResult.results[0].place?.label
            searchSuggestionData.distance =
                searchPlaceIndexForPositionResult.results[0].distance
            searchSuggestionData.isDestination = true
            searchSuggestionData.placeId =
                searchPlaceIndexForPositionResult.results[0].placeId
            searchSuggestionData.isPlaceIndexForPosition = false
            val amazonLocationPlace = Place {
                label = searchPlaceIndexForPositionResult.results[0].place?.label
                geometry = aws.sdk.kotlin.services.location.model.PlaceGeometry {
                    point = listOf(it.longitude!!, it.latitude!!)
                }
                addressNumber = searchPlaceIndexForPositionResult.results[0].place?.addressNumber
                street = searchPlaceIndexForPositionResult.results[0].place?.street
                country = searchPlaceIndexForPositionResult.results[0].place?.country
                region = searchPlaceIndexForPositionResult.results[0].place?.region
                subRegion = searchPlaceIndexForPositionResult.results[0].place?.subRegion
                municipality = searchPlaceIndexForPositionResult.results[0].place?.municipality
                neighborhood = searchPlaceIndexForPositionResult.results[0].place?.neighborhood
                postalCode = searchPlaceIndexForPositionResult.results[0].place?.postalCode
            }
            searchSuggestionData.amazonLocationPlace = amazonLocationPlace
        }
        return searchSuggestionData
    }
}
