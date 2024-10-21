package com.aws.amazonlocation.viewmodel.mapstyle

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.MapStyleData
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
class MapStyleVMSetEsriAndHereListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mMapStyleViewModel: MapStyleViewModel

    override fun setUp() {
        super.setUp()
        mMapStyleViewModel = MapStyleViewModel()
    }

    @Test
    fun setEsriAndHereSuccess() = runTest {
        mMapStyleViewModel.mStyleList = arrayListOf()
        mMapStyleViewModel.mStyleListForFilter = arrayListOf()
        mMapStyleViewModel.providerOptions = arrayListOf()
        mMapStyleViewModel.attributeOptions = arrayListOf()
        mMapStyleViewModel.typeOptions = arrayListOf()
        mMapStyleViewModel.setMapListData(context, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, mMapStyleViewModel.mStyleList.size == 3)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, mMapStyleViewModel.mStyleListForFilter.size == 3)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, mMapStyleViewModel.providerOptions.size == 3)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, mMapStyleViewModel.attributeOptions.size == 5)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, mMapStyleViewModel.typeOptions.size == 2)
    }
}
