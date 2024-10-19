package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_5
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfExceptionTest {

    @Test
    fun testTurfException() {
        try {
            TurfException(TEST_DATA_5)
        } catch (e: TurfException) { }
    }
}