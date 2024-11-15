package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.regionList
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UnitsGetApiKeyTest : BaseTest() {
    private val context = RuntimeEnvironment.getApplication().applicationContext

    private val preferenceManager = PreferenceManager(context)
    @Test
    fun getApiKeySuccess() {
        var result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_US_EAST)
        result = Units.getApiKey(null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_US_EAST)
        preferenceManager.setValue(KEY_NEAREST_REGION, regionList[0])
        result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_US_EAST)
        preferenceManager.setValue(KEY_NEAREST_REGION, regionList[1])
        result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_EU_CENTRAL)
        preferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[1])
        result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_EU_CENTRAL)
        preferenceManager.setValue(KEY_SELECTED_REGION, regionDisplayName[2])
        result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_US_EAST)
        preferenceManager.setValue(KEY_SELECTED_REGION, "test")
        result = Units.getApiKey(preferenceManager)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.API_KEY_US_EAST)
    }
}
