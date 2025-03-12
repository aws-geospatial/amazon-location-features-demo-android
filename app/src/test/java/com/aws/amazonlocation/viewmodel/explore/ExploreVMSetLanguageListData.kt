package com.aws.amazonlocation.viewmodel.explore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED
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
class ExploreVMSetLanguageListData : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    private lateinit var mExploreVM: ExploreViewModel

    @Mock
    private lateinit var locationSearchUseCase: LocationSearchUseCase

    override fun setUp() {
        super.setUp()
        mExploreVM = ExploreViewModel(locationSearchUseCase)
    }

    @Test
    fun setLanguageListDataSuccess() = runTest {
        mExploreVM.mMapLanguageData = arrayListOf()
        mExploreVM.setMapLanguageData(context)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mExploreVM.mMapLanguageData.size == 70
        )
        val languageData = LanguageData("test", "test", false)
        mExploreVM.mMapLanguageData.add(languageData)

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mExploreVM.mMapLanguageData.size == 71
        )

        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED,
            mExploreVM.mMapLanguageData.last().value == languageData.value &&
                mExploreVM.mMapLanguageData.last().label == languageData.label &&
                mExploreVM.mMapLanguageData.last().isSelected == languageData.isSelected
        )
    }
}
