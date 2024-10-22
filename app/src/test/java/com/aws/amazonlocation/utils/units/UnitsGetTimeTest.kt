package com.aws.amazonlocation.utils.units

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UnitsGetTimeTest : BaseTest() {
    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun getTimeSuccess() {
        var result = Units.getTime(context, UNIT_TIME_SEC_1)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_TIME_1_OUTPUT)
        result = Units.getTime(context, UNIT_TIME_SEC_2)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_TIME_2_OUTPUT)
        result = Units.getTime(context, UNIT_TIME_SEC_3)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_TIME_3_OUTPUT)
    }
}
