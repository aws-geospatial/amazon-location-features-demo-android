package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.getUserName
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsGetUserNameTest : BaseTest() {

    @Test
    fun getUserNameSuccess() {
        val loginResponse = Responses.RESPONSE_SIGN_IN
        var result = getUserName(loginResponse)
        var expected = Responses.RESPONSE_SIGN_IN.name?.first()?.uppercase()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)
        loginResponse.name = "Test Test"
        result = getUserName(loginResponse)
        expected = Responses.RESPONSE_SIGN_IN.name?.first()?.uppercase()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected+"T")
        loginResponse.name = null
        result = getUserName(loginResponse)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == null)
        result = getUserName(null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == null)
    }
}
