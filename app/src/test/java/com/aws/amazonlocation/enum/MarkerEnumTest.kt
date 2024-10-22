package com.aws.amazonlocation.enum

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.enum.MarkerEnum
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkerEnumTest : BaseTest() {
    @Test
    fun `Enum values should be correctly defined`() {
        assertEquals("NONE", MarkerEnum.NONE.name)
        assertEquals("DIRECTION_ICON", MarkerEnum.DIRECTION_ICON.name)
        assertEquals("ORIGIN_ICON", MarkerEnum.ORIGIN_ICON.name)
        assertEquals("TRACKER_ICON", MarkerEnum.TRACKER_ICON.name)
        assertEquals("GEOFENCE_ICON", MarkerEnum.GEOFENCE_ICON.name)
        assertEquals("GEOFENCE_DRAG_ICON", MarkerEnum.GEOFENCE_DRAG_ICON.name)
    }

    @Test
    fun `Enum values should have the correct ordinal`() {
        assertEquals(0, MarkerEnum.NONE.ordinal)
        assertEquals(1, MarkerEnum.DIRECTION_ICON.ordinal)
        assertEquals(2, MarkerEnum.ORIGIN_ICON.ordinal)
        assertEquals(3, MarkerEnum.TRACKER_ICON.ordinal)
        assertEquals(4, MarkerEnum.GEOFENCE_ICON.ordinal)
        assertEquals(5, MarkerEnum.GEOFENCE_DRAG_ICON.ordinal)
    }

    @Test
    fun `Enum values should be retrievable by their names`() {
        assertEquals(MarkerEnum.NONE, MarkerEnum.valueOf("NONE"))
        assertEquals(MarkerEnum.DIRECTION_ICON, MarkerEnum.valueOf("DIRECTION_ICON"))
        assertEquals(MarkerEnum.ORIGIN_ICON, MarkerEnum.valueOf("ORIGIN_ICON"))
        assertEquals(MarkerEnum.TRACKER_ICON, MarkerEnum.valueOf("TRACKER_ICON"))
        assertEquals(MarkerEnum.GEOFENCE_ICON, MarkerEnum.valueOf("GEOFENCE_ICON"))
        assertEquals(MarkerEnum.GEOFENCE_DRAG_ICON, MarkerEnum.valueOf("GEOFENCE_DRAG_ICON"))
    }
}