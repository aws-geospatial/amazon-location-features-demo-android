package com.aws.amazonlocation.enum

import com.aws.amazonlocation.data.enum.RedirectionType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RedirectionTypeTest {

    @Test
    fun `Enum values should be correctly defined`() {
        assertEquals("ROUTE_OPTION", RedirectionType.ROUTE_OPTION.name)
        assertEquals("SEARCH_DIRECTION_CAR", RedirectionType.SEARCH_DIRECTION_CAR.name)
        assertEquals("MY_LOCATION", RedirectionType.MY_LOCATION.name)
        assertEquals("DIRECTION_PERMISSION", RedirectionType.DIRECTION_PERMISSION.name)
    }

    @Test
    fun `Enum values should have the correct ordinal`() {
        assertEquals(0, RedirectionType.ROUTE_OPTION.ordinal)
        assertEquals(1, RedirectionType.SEARCH_DIRECTION_CAR.ordinal)
        assertEquals(2, RedirectionType.MY_LOCATION.ordinal)
        assertEquals(3, RedirectionType.DIRECTION_PERMISSION.ordinal)
    }

    @Test
    fun `Enum values should be retrievable by their names`() {
        assertEquals(RedirectionType.ROUTE_OPTION, RedirectionType.valueOf("ROUTE_OPTION"))
        assertEquals(RedirectionType.SEARCH_DIRECTION_CAR, RedirectionType.valueOf("SEARCH_DIRECTION_CAR"))
        assertEquals(RedirectionType.MY_LOCATION, RedirectionType.valueOf("MY_LOCATION"))
        assertEquals(RedirectionType.DIRECTION_PERMISSION, RedirectionType.valueOf("DIRECTION_PERMISSION"))
    }
}