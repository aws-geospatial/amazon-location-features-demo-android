package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.regionList
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsDefaultIdentityPoolIdTest : BaseTest() {

    @Test
    fun toDefaultIdentityPoolIdSuccess() {
        var result = Units.getDefaultIdentityPoolId(regionDisplayName[0], "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[0], regionList[0])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[0], regionList[1])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[0], regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP)
        result = Units.getDefaultIdentityPoolId("", regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[1], regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[2], regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[3], regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID)
    }
}
