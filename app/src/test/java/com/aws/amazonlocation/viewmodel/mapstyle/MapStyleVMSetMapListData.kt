package com.aws.amazonlocation.viewmodel.mapstyle

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_STANDARD
import com.aws.amazonlocation.ui.main.mapStyle.MapStyleViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MapStyleVMSetMapListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mMapStyleViewModel: MapStyleViewModel

    override fun setUp() {
        super.setUp()
        mMapStyleViewModel = MapStyleViewModel()
    }

    @Test
    fun setMapListDataSuccess() = runTest {
        mMapStyleViewModel.mStyleList = arrayListOf()
        mMapStyleViewModel.setMapListData(context)
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_STANDARD,
            mMapStyleViewModel.mStyleList.size == 1
        )
    }
}
