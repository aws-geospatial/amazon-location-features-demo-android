package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED
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
class ExploreVMSetPoliticalListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var locationSearchUseCase: LocationSearchUseCase

    private lateinit var mExploreVM: ExploreViewModel

    override fun setUp() {
        super.setUp()

        mExploreVM = ExploreViewModel(locationSearchUseCase)
    }

    @Test
    fun setPoliticalListDataSuccess() = runTest {
        mExploreVM.setPoliticalListData(context)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mExploreVM.mPoliticalData.size == 14
        )
        val testPoliticalData = PoliticalData("testCountry", "description", "AR", false)
        mExploreVM.mPoliticalData.add(testPoliticalData)
        mExploreVM.mPoliticalSearchData.add(testPoliticalData)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mExploreVM.mPoliticalData.size == 15
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mExploreVM.mPoliticalData.last().countryName == testPoliticalData.countryName &&
                    mExploreVM.mPoliticalData.last().description == testPoliticalData.description&&
                    mExploreVM.mPoliticalData.last().countryCode == testPoliticalData.countryCode&&
                    mExploreVM.mPoliticalData.last().isSelected == testPoliticalData.isSelected
        )

        val result = mExploreVM.searchPoliticalData("test")
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            result.size == 1
        )
    }
}
