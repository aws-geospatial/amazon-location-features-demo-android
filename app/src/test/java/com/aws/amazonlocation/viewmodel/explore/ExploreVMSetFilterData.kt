package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ExploreVMSetFilterData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var locationSearchUseCase: LocationSearchUseCase

    private lateinit var mExploreVM: ExploreViewModel

    override fun setUp() {
        super.setUp()
        mExploreVM = ExploreViewModel(locationSearchUseCase)
        mExploreVM.setMapListData(context, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setExploreVMFilterData() = runTest {
        val result = mExploreVM.filterAndSortItems(context, LIGHT, null, null, null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result.size == 4)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setExploreProviderData() = runTest {
        val result = mExploreVM.filterAndSortItems(context, null, listOf(ESRI), null, null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 6)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setExploreAttributeData() = runTest {
        val result = mExploreVM.filterAndSortItems(context, null, null, listOf(LIGHT), null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 4)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setExploreTypeData() = runTest {
        val result = mExploreVM.filterAndSortItems(context, null, null, null, listOf(VECTOR))
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI, result[0].mapInnerData?.size == 5)
    }
}
