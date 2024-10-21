package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.setLocale
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsGetLanguageTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().baseContext

    @Test
    fun isInternetAvailableSuccess() {
        val result = getLanguageCode()
        result?.let { setLocale(it, context) }
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == "en")
    }
}
