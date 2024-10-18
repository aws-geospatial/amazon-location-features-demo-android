package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsToLowerUnitTest : BaseTest() {

    @Test
    fun toLowerUnitSuccess() {
        var result = Units.convertToLowerUnit(UNIT_KM_TO_M_INPUT, true)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_KM_TO_M_OUTPUT)
        result = Units.convertToLowerUnit(UNIT_MI_TO_FT_INPUT, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_MI_TO_FT_OUTPUT)
    }
}
