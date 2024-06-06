package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.validateLatLng
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateLatLngTest : BaseTest() {

    @Test
    fun validateLatLngSuccess() {
        val result = validateLatLng(LAT_LNG_VALID_STRING)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INVALID_LATITUDE, result?.latitude == VALID_LAT)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INVALID_LONGITUDE, result?.longitude == VALID_LNG)
    }

    @Test
    fun testValidateLatLngInvalidLat() {
        val result = validateLatLng(LAT_LNG_VALID_LNG_STRING)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INVALID_LATITUDE, result == null)
    }

    @Test
    fun testValidateLatLngInvalidLng() {
        val result = validateLatLng("")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INVALID_LONGITUDE, result == null)
    }
}
