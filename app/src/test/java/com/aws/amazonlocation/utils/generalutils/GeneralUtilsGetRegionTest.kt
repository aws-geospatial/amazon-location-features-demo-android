package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.getRegion
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsGetRegionTest : BaseTest() {

    @Test
    fun getRegionSuccess() {
        var result = getRegion(null, null, null)
        var expected = GET_REGION_EMPTY
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)

        result = getRegion(GET_REGION_REGION, null, GET_REGION_COUNTRY)
        expected = GET_REGION_REGION_COUNTRY
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)

        result = getRegion(null, GET_REGION_SUBREGION, GET_REGION_COUNTRY)
        expected = GET_REGION_SUBREGION_COUNTRY
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)

        result = getRegion(null, null, GET_REGION_COUNTRY)
        expected = GET_REGION_COUNTRY
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)
    }
}
