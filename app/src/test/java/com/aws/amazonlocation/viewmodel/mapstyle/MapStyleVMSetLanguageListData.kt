package com.aws.amazonlocation.viewmodel.mapstyle

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED
import com.aws.amazonlocation.ui.main.mapStyle.MapStyleViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MapStyleVMSetLanguageListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mMapStyleViewModel: MapStyleViewModel

    override fun setUp() {
        super.setUp()
        mMapStyleViewModel = MapStyleViewModel()
    }

    @Test
    fun setLanguageListDataSuccess() = runTest {
        mMapStyleViewModel.mMapLanguageData = arrayListOf()
        mMapStyleViewModel.setMapLanguageData(context)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mMapStyleViewModel.mMapLanguageData.size == 70
        )
        val languageData = LanguageData("test", "test", false)
        mMapStyleViewModel.mMapLanguageData.add(languageData)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mMapStyleViewModel.mMapLanguageData.size == 71
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mMapStyleViewModel.mMapLanguageData.last().value == languageData.value &&
                mMapStyleViewModel.mMapLanguageData.last().label == languageData.label &&
                mMapStyleViewModel.mMapLanguageData.last().isSelected == languageData.isSelected
        )
    }
}
