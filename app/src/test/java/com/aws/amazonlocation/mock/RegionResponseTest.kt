package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.RegionResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegionResponseTest {

    private lateinit var regionResponse: RegionResponse

    @Before
    fun setUp() {
        regionResponse = RegionResponse(
            name = REGION_NAME,
            isSelected = true
        )
        regionResponse.name = REGION_NAME
        regionResponse.isSelected = true
    }

    @Test
    fun testName() {
        val expectedName = REGION_NAME
        assertEquals(expectedName, regionResponse.name)
    }

    @Test
    fun testIsSelected() {
        assertEquals(true, regionResponse.isSelected)
    }
}
