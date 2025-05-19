package com.aws.amazonlocation.utils.preferencemanager

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.DELAY_1000
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_BOOL
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_DOUBLE
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_INT
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_LONG
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_STRING
import com.aws.amazonlocation.mock.PREF_MANAGER_KEY_STRING_SET
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_BOOL
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_DOUBLE
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_INT
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_LONG
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_STRING
import com.aws.amazonlocation.mock.PREF_MANAGER_VALUE_STRING_SET
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.PreferenceManager
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PreferenceManagerSetValueGetValueTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    private val preferenceManager = PreferenceManager(context)

    @Test
    fun setValueGetValueSuccess() {
        Thread.sleep(DELAY_1000)
        preferenceManager.setValue(PREF_MANAGER_KEY_STRING, PREF_MANAGER_VALUE_STRING)
        var string = preferenceManager.getValue(PREF_MANAGER_KEY_STRING, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, string == PREF_MANAGER_VALUE_STRING)
        preferenceManager.setValue(PREF_MANAGER_KEY_INT, PREF_MANAGER_VALUE_INT)
        val int = preferenceManager.getIntValue(PREF_MANAGER_KEY_INT, 0)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, int == PREF_MANAGER_VALUE_INT)
        preferenceManager.setValue(PREF_MANAGER_KEY_BOOL, PREF_MANAGER_VALUE_BOOL)
        var bool = preferenceManager.getBooleanValue(PREF_MANAGER_KEY_BOOL, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, bool == PREF_MANAGER_VALUE_BOOL)
        bool = preferenceManager.getValue(PREF_MANAGER_KEY_BOOL, false)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, bool == PREF_MANAGER_VALUE_BOOL)

        preferenceManager.setValue(PREF_MANAGER_KEY_DOUBLE, PREF_MANAGER_VALUE_DOUBLE)
        preferenceManager.setValue(PREF_MANAGER_KEY_LONG, PREF_MANAGER_VALUE_LONG)
        preferenceManager.setValue(PREF_MANAGER_KEY_STRING_SET, PREF_MANAGER_VALUE_STRING_SET)

        preferenceManager.removeValue(PREF_MANAGER_KEY_STRING)
        string = preferenceManager.getValue(PREF_MANAGER_KEY_STRING, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, string.isNullOrBlank())
    }
}
