package com.aws.amazonlocation.mock

import com.aws.amazonlocation.ui.main.explore.FilterOption
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilterOptionTest {

    @Test
    fun `FilterOption with default values`() {
        val filterOption = FilterOption(option1)

        assertEquals(option1, filterOption.name)
        assertEquals(false, filterOption.isSelected)
        assertEquals(false, filterOption.isApplyFilter)
    }

    @Test
    fun `FilterOption with custom values`() {
        val filterOption = FilterOption(option2, isSelected = true, isApplyFilter = true)

        assertEquals(option2, filterOption.name)
        assertEquals(true, filterOption.isSelected)
        assertEquals(true, filterOption.isApplyFilter)
    }

    @Test
    fun `FilterOption equality`() {
        val filterOption1 = FilterOption(option3, isSelected = true, isApplyFilter = true)
        val filterOption2 = FilterOption(option3, isSelected = true, isApplyFilter = true)

        assertEquals(filterOption1, filterOption2)
    }

    @Test
    fun `FilterOption inequality`() {
        val filterOption1 = FilterOption(option4, isSelected = true, isApplyFilter = true)
        val filterOption2 = FilterOption(option5, isSelected = false, isApplyFilter = false)
        assertEquals(false, filterOption1 == filterOption2)
    }
}
