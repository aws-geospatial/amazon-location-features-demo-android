package com.aws.amazonlocation.utils.preferencemanager

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PreferenceManagerSetDefaultConfigTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    private val preferenceManager = PreferenceManager(context)

    @Test
    fun setDefaultConfigSuccess() {
        preferenceManager.setDefaultConfig()
        Thread.sleep(DELAY_1000)
        val poolId = preferenceManager.getValue(KEY_POOL_ID, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, poolId.isNullOrBlank())
        val userPoolId = preferenceManager.getValue(KEY_USER_POOL_ID, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, userPoolId.isNullOrBlank())
        val userPoolClientId = preferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, userPoolClientId.isNullOrBlank())
        val cloudFormationStatus = preferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, AuthEnum.DEFAULT.name)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, cloudFormationStatus == AuthEnum.DEFAULT.name)
        val userRegion = preferenceManager.getValue(KEY_USER_REGION, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, userRegion.isNullOrBlank())
        val userDomain = preferenceManager.getValue(KEY_USER_DOMAIN, "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, userDomain.isNullOrBlank())
    }
}
