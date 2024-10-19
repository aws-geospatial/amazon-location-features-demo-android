package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config()
class UnitsCountryToUnitSystemTest : BaseTest() {

    @Test
    fun toLowerUnitSuccess() {
        Locale.setDefault(LOCALE_US)
        var result = Units.isMetricUsingCountry()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        Locale.setDefault(LOCALE_IN)
        result = Units.isMetricUsingCountry()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
    }
}
