
package com.aws.amazonlocation.utils.enum_test

import com.aws.amazonlocation.data.enum.TabEnum
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TabEnumTest {

    @Test
    fun `Enum values should be correctly defined`() {
        assertEquals("TAB_EXPLORE", TabEnum.TAB_EXPLORE.name)
        assertEquals("TAB_TRACKING", TabEnum.TAB_TRACKING.name)
        assertEquals("TAB_GEOFENCE", TabEnum.TAB_GEOFENCE.name)
    }

    @Test
    fun `Enum values should have the correct ordinal`() {
        assertEquals(0, TabEnum.TAB_EXPLORE.ordinal)
        assertEquals(1, TabEnum.TAB_TRACKING.ordinal)
        assertEquals(2, TabEnum.TAB_GEOFENCE.ordinal)
    }

    @Test
    fun `Enum values should be retrievable by their names`() {
        assertEquals(TabEnum.TAB_EXPLORE, TabEnum.valueOf("TAB_EXPLORE"))
        assertEquals(TabEnum.TAB_TRACKING, TabEnum.valueOf("TAB_TRACKING"))
        assertEquals(TabEnum.TAB_GEOFENCE, TabEnum.valueOf("TAB_GEOFENCE"))
    }
}
