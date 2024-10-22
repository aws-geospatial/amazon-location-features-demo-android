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
class UnitsGetMetricsTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext
    @Test
    fun getMetricsSuccess() {
        var result = Units.getMetricsNew(context, UNIT_METRICS_M_INPUT_1, true)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_M_OUTPUT_2)
        result = Units.getMetricsNew(context, UNIT_METRICS_M_INPUT_2, true)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_M_OUTPUT_2)
        result = Units.getMetricsNew(context, UNIT_METRICS_I_INPUT_1, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_I_OUTPUT_2)
        result = Units.getMetricsNew(context, UNIT_METRICS_I_INPUT_2, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_I_OUTPUT_2)
    }
}
