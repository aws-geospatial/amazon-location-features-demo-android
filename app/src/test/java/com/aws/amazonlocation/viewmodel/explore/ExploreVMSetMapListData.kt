package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.STANDARD
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_STANDARD
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_STANDARD
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

    @Test
    fun setMapListDataSuccess() = runTest {
        mExploreVM.mStyleList = arrayListOf()
        mExploreVM.setMapListData(context)

        mExploreVM.mStyleList[0].styleNameDisplay = STANDARD
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
            mExploreVM.mStyleList.size == 1
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_STANDARD,
            mExploreVM.mStyleList[0].styleNameDisplay == STANDARD
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_STANDARD,
            mExploreVM.mStyleList[0].mapInnerData?.size == 4
        )
    }
}
