package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsKiloMeterToMeterTest : BaseTest() {

    @Test
    fun kiloMeterToMeterSuccess() {
        val result = Units.kiloMeterToMeter(UNIT_KM_TO_M_INPUT)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_KM_TO_M_OUTPUT)
    }
}
