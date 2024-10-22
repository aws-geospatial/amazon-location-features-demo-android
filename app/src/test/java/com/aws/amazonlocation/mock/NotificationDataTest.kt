package com.aws.amazonlocation.mock

import com.aws.amazonlocation.ui.main.simulation.NotificationData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationDataTest {

    @Test
    fun testName() {
        val notificationData = NotificationData(name = TEST_NOTIFICATION)
        assert(notificationData.name == TEST_NOTIFICATION)
    }

    @Test
    fun testIsSelected() {
        val notificationData = NotificationData(name = TEST_NOTIFICATION, isSelected = true)
        notificationData.isSelected = notificationData.isSelected
        assert(notificationData.isSelected)
    }
}