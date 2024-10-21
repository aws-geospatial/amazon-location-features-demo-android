package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.AUTOMATIC
import com.aws.amazonlocation.mock.IMPERIAL
import com.aws.amazonlocation.mock.LOCALE_IN
import com.aws.amazonlocation.mock.METRIC
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.Units
import java.util.Locale
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config()
class UnitsIsMetricTest : BaseTest() {

    @Test
    fun getDistanceUnitSuccess() {
        Locale.setDefault(Locale.US)
        var result = Units.isMetric(AUTOMATIC)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        Locale.setDefault(LOCALE_IN)
        result = Units.isMetric(AUTOMATIC)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = Units.isMetric(IMPERIAL)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = Units.isMetric(METRIC)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
    }
}
