package com.aws.amazonlocation.utils.enum_test

import com.aws.amazonlocation.data.enum.SearchApiEnum
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchApiEnumTest {

    @Test
    fun `Enum values should be correctly defined`() {
        assertEquals("SEARCH_PLACE_SUGGESTION", SearchApiEnum.SEARCH_PLACE_SUGGESTION.name)
        assertEquals("SEARCH_PLACE_INDEX_TEXT", SearchApiEnum.SEARCH_PLACE_INDEX_TEXT.name)
    }

    @Test
    fun `Enum values should have the correct ordinal`() {
        assertEquals(0, SearchApiEnum.SEARCH_PLACE_SUGGESTION.ordinal)
        assertEquals(1, SearchApiEnum.SEARCH_PLACE_INDEX_TEXT.ordinal)
    }

    @Test
    fun `Enum values should be retrievable by their names`() {
        assertEquals(SearchApiEnum.SEARCH_PLACE_SUGGESTION, SearchApiEnum.valueOf("SEARCH_PLACE_SUGGESTION"))
        assertEquals(SearchApiEnum.SEARCH_PLACE_INDEX_TEXT, SearchApiEnum.valueOf("SEARCH_PLACE_INDEX_TEXT"))
    }
}
