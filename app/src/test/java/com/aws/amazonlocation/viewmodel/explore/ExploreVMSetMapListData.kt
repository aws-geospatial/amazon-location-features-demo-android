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
class ExploreVMSetMapListData : BaseTest() {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setMapListDataSuccess() = runTest {
        mExploreVM.setMapListData(context, true)

        mExploreVM.mStyleList[0].styleNameDisplay = ESRI
        mExploreVM.mStyleList[0].isSelected = mExploreVM.mStyleList[0].isSelected
        mExploreVM.mStyleList[0].mapInnerData?.get(0)?.image
        mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mMapName =
            mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mMapName
        mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mapName =
            mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mapName
        mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mMapStyleName =
            mExploreVM.mStyleList[0].mapInnerData?.get(0)?.mMapStyleName
        mExploreVM.mStyleList[0].mapInnerData?.get(0)?.isSelected = false

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED,
            mExploreVM.mStyleList.size == 4
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_ESRI,
            mExploreVM.mStyleList[0].styleNameDisplay == ESRI
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_ESRI,
            mExploreVM.mStyleList[2].styleNameDisplay == GRAB
        )
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI,
            mExploreVM.mStyleList[0].mapInnerData?.size == 6
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_HERE,
            mExploreVM.mStyleList[1].styleNameDisplay == HERE
        )
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_HERE,
            mExploreVM.mStyleList[1].mapInnerData?.size == 5
        )
    }
}
