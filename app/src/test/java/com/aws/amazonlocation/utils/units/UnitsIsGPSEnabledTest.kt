package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.setGPSEnabled
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UnitsIsGPSEnabledTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun isGPSEnabledSuccess() {
        setGPSEnabled(true)
        var result = Units.isGPSEnabled(context)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        setGPSEnabled(false)
        result = Units.isGPSEnabled(context)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
