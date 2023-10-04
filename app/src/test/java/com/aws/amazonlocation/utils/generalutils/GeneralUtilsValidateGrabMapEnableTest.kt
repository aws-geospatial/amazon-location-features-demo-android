package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isGrabMapSelected
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateGrabMapEnableTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun validateGrabEnableSuccess() {
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, SE_REGION)
        var result = isGrabMapEnable(mPreferenceManager)
        Assert.assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            result
        )
        mPreferenceManager.setValue(KEY_CLOUD_FORMATION_STATUS, AuthEnum.AWS_CONNECTED.name)
        result = isGrabMapEnable(mPreferenceManager)
        Assert.assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            result
        )
    }

    @Test
    fun validateGrabSelectedSuccess() {
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_MAP_NAME, GRAB)
        val result = isGrabMapSelected(mPreferenceManager, context)
        Assert.assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_SELECTED,
            result
        )
    }
}
