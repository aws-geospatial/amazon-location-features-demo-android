package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningRemoteDataSourceImplTest
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.isRunningTest2LiveLocation
import com.aws.amazonlocation.utils.isRunningTest3LiveLocation
import com.aws.amazonlocation.utils.isRunningTestLiveLocation
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsIsInternetAvailableTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().baseContext

    @Test
    fun isInternetAvailableSuccess() {
        setConnectivity(true)
        var result = context.isInternetAvailable()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        setConnectivity(false)
        result = context.isInternetAvailable()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }

    @Test
    fun isUnitTestRunningCheck() {
        Assert.assertTrue(TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING, !isRunningTest)
        Assert.assertTrue(TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING, !isRunningTestLiveLocation)
        Assert.assertTrue(TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING, !isRunningTest2LiveLocation)
        Assert.assertTrue(TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING, !isRunningTest3LiveLocation)
        Assert.assertTrue(TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING, !isRunningRemoteDataSourceImplTest)
    }
}
