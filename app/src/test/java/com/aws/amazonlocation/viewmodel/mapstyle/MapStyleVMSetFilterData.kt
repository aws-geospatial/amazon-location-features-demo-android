package com.aws.amazonlocation.viewmodel.mapstyle

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.map_style.MapStyleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MapStyleVMSetFilterData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mMapStyleViewModel: MapStyleViewModel

    override fun setUp() {
        super.setUp()
        mMapStyleViewModel = MapStyleViewModel()
        mMapStyleViewModel.setMapListData(context, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setMapStyleVMFilterData() = runTest {
        val result = mMapStyleViewModel.filterAndSortItems(context, LIGHT, null, null, null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result.size == 4)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setMapStyleProviderData() = runTest {
        val result = mMapStyleViewModel.filterAndSortItems(context, null, listOf(ESRI), null, null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 6)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setMapStyleAttributeData() = runTest {
        val result = mMapStyleViewModel.filterAndSortItems(context, null, null, listOf(LIGHT), null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 4)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setMapStyleTypeData() = runTest {
        val result = mMapStyleViewModel.filterAndSortItems(context, null, null, null, listOf(VECTOR))
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 5)
    }
}
