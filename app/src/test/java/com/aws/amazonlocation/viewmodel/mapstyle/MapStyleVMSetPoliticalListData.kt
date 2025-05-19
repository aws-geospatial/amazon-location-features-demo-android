package com.aws.amazonlocation.viewmodel.mapstyle

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.PoliticalData
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED
import com.aws.amazonlocation.ui.main.mapStyle.MapStyleViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MapStyleVMSetPoliticalListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mMapStyleViewModel: MapStyleViewModel

    override fun setUp() {
        super.setUp()
        mMapStyleViewModel = MapStyleViewModel()
    }

    @Test
    fun setPoliticalListDataSuccess() = runTest {
        mMapStyleViewModel.mPoliticalData = arrayListOf()
        mMapStyleViewModel.mPoliticalSearchData = arrayListOf()
        mMapStyleViewModel.setPoliticalListData(context)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mMapStyleViewModel.mPoliticalData.size == 18
        )
        val testPoliticalData = PoliticalData("testCountry", "description", "AR", false)
        mMapStyleViewModel.mPoliticalData.add(testPoliticalData)
        mMapStyleViewModel.mPoliticalSearchData.add(testPoliticalData)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mMapStyleViewModel.mPoliticalData.size == 19
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mMapStyleViewModel.mPoliticalData.last().countryName == testPoliticalData.countryName &&
                mMapStyleViewModel.mPoliticalData.last().description == testPoliticalData.description &&
                mMapStyleViewModel.mPoliticalData.last().countryCode == testPoliticalData.countryCode &&
                mMapStyleViewModel.mPoliticalData.last().isSelected == testPoliticalData.isSelected
        )

        val result = mMapStyleViewModel.searchPoliticalData("test")
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            result.size == 1
        )
    }
}
