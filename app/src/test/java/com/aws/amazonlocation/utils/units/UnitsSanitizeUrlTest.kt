package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsSanitizeUrlTest : BaseTest() {

    @Test
    fun checkSanitizeUrlSuccess() {
        val result = Units.sanitizeUrl(BuildConfig.AWS_CUSTOMER_AGREEMENT)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result.endsWith("/"))
    }

    @Test
    fun checkMeterToFeetSuccess() {
        val result = Units.meterToFeet(50.5)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == 165.68241495)
    }
}
