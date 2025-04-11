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
    fun testDestinationAddress() {
        assertEquals(DESTINATION, navigationResponse.destinationAddress)
    }

    @Test
    fun testNavigationList() {
        assertEquals(ArrayList<NavigationData>(), navigationResponse.navigationList)
    }

    @Test
    fun testGetAddressWithDataSuccess() {
        // `getAddress()` should return `destinationAddress` directly, regardless of other fields.
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
        val result = navigationData.getAddress()

        assertEquals(CITY_NAME, result)
    }

    @Test
    fun testGetAddressWithDataSuccessSubRegion() {
        // `getAddress()` should return `country` if both are available.
        val navigationData = NavigationData(
            subRegion = SUB_REGION_NAME,
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getAddress()

        assertEquals(COUNTRY_NAME, result)
    }

    @Test
    fun testGetAddressWithDataSuccessRegion() {
        // `getAddress()` should concatenate `region, country` as the result.
        val navigationData = NavigationData(
            region = REGION_NAME,
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getAddress()

        assertEquals("$REGION_NAME, $COUNTRY_NAME", result)
    }

    @Test
    fun testGetAddressWithDataSuccessCountry() {
        // `getAddress()` should return the `country` value as the address.
        val navigationData = NavigationData(
            country = COUNTRY_NAME,
            isDataSuccess = true
        )

        val result = navigationData.getAddress()

        assertEquals(COUNTRY_NAME, result)
    }
}
