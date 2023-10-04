package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class NavigationDataTest {
    private lateinit var navigationResponse: NavigationResponse

    @Before
    fun setUp() {
        navigationResponse = NavigationResponse(
            duration = hour_1,
            distance = 10.0,
            startLat = 0.0,
            startLng = 0.0,
            endLat = 1.0,
            endLng = 1.0,
            destinationAddress = DESTINATION,
            navigationList = ArrayList()
        )
    }

    @Test
    fun testDuration() {
        assertEquals(hour_1, navigationResponse.duration)
    }

    @Test
    fun testDistance() {
        navigationResponse.distance?.let { assertEquals(10.0, it, 0.0) }
    }

    @Test
    fun testStartLat() {
        navigationResponse.startLat?.let { assertEquals(0.0, it, 0.0) }
    }

    @Test
    fun testStartLng() {
        navigationResponse.startLng?.let { assertEquals(0.0, it, 0.0) }
    }

    @Test
    fun testEndLat() {
        navigationResponse.endLat?.let { assertEquals(1.0, it, 0.0) }
    }

    @Test
    fun testEndLng() {
        navigationResponse.endLng?.let { assertEquals(1.0, it, 0.0) }
    }

    @Test
    fun testDestinationAddress() {
        assertEquals(DESTINATION, navigationResponse.destinationAddress)
    }

    @Test
    fun testNavigationList() {
        assertEquals(ArrayList<NavigationData>(), navigationResponse.navigationList)
    }

    @Test
    fun testGetRegionsWithDataSuccess() {
        val navigationData = NavigationData(
            destinationAddress = CITY_NAME,
            region = REGION_NAME,
            subRegion = SUB_REGION_NAME,
            country = COUNTRY_NAME,
            isDataSuccess = true
        )
        navigationData.duration = 10.0
        navigationData.distance = 10.0
        navigationData.startLat = 21.5511451
        navigationData.startLng = 21.5511451
        navigationData.endLat = 21.5511451
        navigationData.endLng = 21.5511451
        navigationData.isDataSuccess = true
        val result = navigationData.getRegions()

        assertEquals(CITY_NAME, result)
    }

    @Test
    fun testGetRegionsWithDataSuccessSubRegion() {
        val navigationData = NavigationData(
            subRegion = SUB_REGION_NAME,
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getRegions()

        assertEquals(COUNTRY_NAME, result)
    }

    @Test
    fun testGetRegionsWithDataSuccessRegion() {
        val navigationData = NavigationData(
            region = REGION_NAME,
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getRegions()

        assertEquals("$REGION_NAME, $COUNTRY_NAME", result)
    }

    @Test
    fun testGetRegionsWithDataSuccessCountry() {
        val navigationData = NavigationData(
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getRegions()

        assertEquals(COUNTRY_NAME, result)
    }

    @Test
    fun testGetRegionsWithDataFailure() {
        val navigationData = NavigationData(
            destinationAddress = CITY_NAME,
            isDataSuccess = false
        )

        val result = navigationData.getRegions()

        assertEquals(CITY_NAME, result)
    }
}