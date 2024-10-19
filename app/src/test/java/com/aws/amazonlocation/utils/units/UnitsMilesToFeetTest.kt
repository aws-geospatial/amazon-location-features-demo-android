package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsMilesToFeetTest : BaseTest() {

    @Test
    fun kiloMeterToMeterSuccess() {
        val result = Units.milesToFeet(UNIT_MI_TO_FT_INPUT)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_MI_TO_FT_OUTPUT)
    }
}
