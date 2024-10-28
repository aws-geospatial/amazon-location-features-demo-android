package com.aws.amazonlocation.data.response

import aws.sdk.kotlin.services.geoplaces.model.Address
import aws.sdk.kotlin.services.geoplaces.model.Country
import aws.sdk.kotlin.services.geoplaces.model.PlaceType
import aws.sdk.kotlin.services.geoplaces.model.Region
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResultItem
import aws.sdk.kotlin.services.geoplaces.model.SubRegion
import com.aws.amazonlocation.mock.STANDARD
import com.aws.amazonlocation.mock.NO_DATA_FOUND
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.TEST_DATA
import com.aws.amazonlocation.mock.TEST_DATA_2
import com.aws.amazonlocation.mock.TEST_DATA_3
import com.aws.amazonlocation.mock.TEST_DATA_4
import com.aws.amazonlocation.mock.TEST_DATA_5
import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.mock.TEST_FAILED_ADD_GEOFENCE_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_CALCULATE_DISTANCE_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_LOGIN_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_MAP_STYLE_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_MAP_STYLE_INNER_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_NAVIGATION_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_SEARCH_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_SEARCH_SUGGESTION_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_SIGN_OUT_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_TRACKING_HISTORY_DATA
import com.aws.amazonlocation.mock.TEST_FAILED_UPDATE_BATCH_DATA
import java.util.Date
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResponseDataTest {

    @Test
    fun navigationDataTest() {
        // `getAddress()` should return `destinationAddress` directly as other fields are empty
        val navigationData = Responses.RESPONSE_NAVIGATION_DATA_CAR_STEP_1
        navigationData.destinationAddress = TEST_DATA
        navigationData.subRegion = ""
        navigationData.region = ""
        navigationData.country = ""
        Assert.assertTrue(TEST_FAILED_NAVIGATION_DATA, navigationData.getAddress() == TEST_DATA)
    }

    @Test
    fun loginDataTest() {
        val loginResponse = Responses.RESPONSE_SIGN_IN
        loginResponse.idToken = loginResponse.idToken
        loginResponse.name = loginResponse.name
        loginResponse.provider = loginResponse.provider
        loginResponse.success = loginResponse.success
        loginResponse.email = loginResponse.email
        Assert.assertTrue(TEST_FAILED_LOGIN_DATA, loginResponse.email == null)
    }

    @Test
    fun searchSuggestionResponseTest() {
        val searchSuggestionResponse = Responses.RESPONSE_SEARCH_TEXT_RIO_TINTO
        searchSuggestionResponse.text = searchSuggestionResponse.text
        searchSuggestionResponse.maxResults = searchSuggestionResponse.maxResults
        searchSuggestionResponse.error = searchSuggestionResponse.error
        searchSuggestionResponse.data[0].isPlaceIndexForPosition
        searchSuggestionResponse.data[0].isDestination
        searchSuggestionResponse.data[0].distance
        searchSuggestionResponse.data[0].text
        searchSuggestionResponse.data[0].searchText
        searchSuggestionResponse.data[0].placeId
        searchSuggestionResponse.data[0].isDestination?.let {
            Assert.assertTrue(
                TEST_FAILED_SEARCH_SUGGESTION_DATA,
                !it
            )
        }
    }

    @Test
    fun searchResponseTest() {
        val searchResponse = Responses.SEARCH_RESPONSE_TAJ
        searchResponse.latitude = TEST_DATA_LAT
        searchResponse.longitude = TEST_DATA_LNG
        searchResponse.reverseGeocodeResponse = ReverseGeocodeResponse {
            pricingBucket = "test"
            resultItems = listOf(
                ReverseGeocodeResultItem {
                    placeId = "test"
                    title = "testTitle"
                    placeType = PlaceType.Block
                    distance = 0L
                    position = listOf(TEST_DATA_LNG, TEST_DATA_LAT)
                    address = Address {
                        country = Country {name = TEST_DATA_2}
                        label = TEST_DATA_3
                        region = Region{name = TEST_DATA_4}
                        subRegion = SubRegion {name = TEST_DATA}
                    }
                }
            )
        }
        Assert.assertTrue(TEST_FAILED_SEARCH_DATA, searchResponse.latitude == TEST_DATA_LAT)
    }

    @Test
    fun updateBatchLocationResponseTest() {
        val updateBatchLocationResponse = UpdateBatchLocationResponse(null, true)
        updateBatchLocationResponse.errorMessage = NO_DATA_FOUND
        updateBatchLocationResponse.isLocationDataAdded = false
        updateBatchLocationResponse.errorMessage = updateBatchLocationResponse.errorMessage
        Assert.assertTrue(
            TEST_FAILED_UPDATE_BATCH_DATA,
            updateBatchLocationResponse.errorMessage == NO_DATA_FOUND
        )
    }

    @Test
    fun addGeofenceResponseTest() {
        val addGeofenceResponse = AddGeofenceResponse(
            isGeofenceDataAdded = true,
            errorMessage = null
        )
        addGeofenceResponse.errorMessage = NO_DATA_FOUND
        addGeofenceResponse.isGeofenceDataAdded = false
        Assert.assertTrue(
            TEST_FAILED_ADD_GEOFENCE_DATA,
            addGeofenceResponse.errorMessage == NO_DATA_FOUND
        )
    }

    @Test
    fun signOutDataTest() {
        val addGeofenceResponse = SignOutData(
            message = null,
            isDisconnectFromAWSRequired = false
        )
        addGeofenceResponse.message = NO_DATA_FOUND
        addGeofenceResponse.isDisconnectFromAWSRequired = false
        Assert.assertTrue(TEST_FAILED_SIGN_OUT_DATA, addGeofenceResponse.message == NO_DATA_FOUND)
    }

    @Test
    fun calculateDistanceResponseTest() {
        val calculateDistanceResponse = CalculateDistanceResponse(
            name = TEST_DATA_5,
            calculateRouteResult = null,
            sourceLatLng = null,
            destinationLatLng = null
        )
        calculateDistanceResponse.name = calculateDistanceResponse.name
        calculateDistanceResponse.calculateRouteResult =
            calculateDistanceResponse.calculateRouteResult
        calculateDistanceResponse.sourceLatLng = calculateDistanceResponse.sourceLatLng
        calculateDistanceResponse.destinationLatLng = calculateDistanceResponse.destinationLatLng
        Assert.assertTrue(
            TEST_FAILED_CALCULATE_DISTANCE_DATA,
            calculateDistanceResponse.name == TEST_DATA_5
        )
    }

    @Test
    fun trackingHistoryDataTest() {
        val data = TrackingHistoryData(
            headerId = TEST_DATA_5,
            headerString = "",
            headerData = "",
            devicePositionData = null
        )
        data.headerId = data.headerId
        data.headerString = data.headerString
        data.headerData = data.headerData
        data.devicePositionData = data.devicePositionData
        Assert.assertTrue(TEST_FAILED_TRACKING_HISTORY_DATA, data.headerId == TEST_DATA_5)
    }

    @Test
    fun mapStyleInnerDataTest() {
        val data = MapStyleInnerData(
            mapName = null,
            isSelected = false,
            image = 0,
            mMapName = null,
            mMapStyleName = null
        )
        Assert.assertTrue(TEST_FAILED_MAP_STYLE_INNER_DATA, !data.isSelected)
    }

    @Test
    fun mapStyleDataTest() {
        val data = MapStyleInnerData(
            mapName = null,
            isSelected = false,
            image = 0,
            mMapName = null,
            mMapStyleName = null
        )

        val mapStyleData = MapStyleData(
            styleNameDisplay = STANDARD,
            isSelected = false,
            mapInnerData = null
        )
        mapStyleData.mapInnerData = arrayListOf(data)
        Assert.assertTrue(TEST_FAILED_MAP_STYLE_DATA, !mapStyleData.isSelected)
    }

    @Test
    fun notificationResponseTest() {
        val navigationResponse = NotificationSimulationData(
            coordinates = listOf(),
            eventTime = "01/01/22",
            geofenceCollection = "test",
            geofenceId = "1",
            source = "test",
            stopName = "test",
            trackerEventType = "test"
        )
        navigationResponse.coordinates = navigationResponse.coordinates
        navigationResponse.eventTime = navigationResponse.eventTime
        navigationResponse.geofenceCollection = navigationResponse.geofenceCollection
        navigationResponse.geofenceId = navigationResponse.geofenceId
        navigationResponse.source = navigationResponse.source
        navigationResponse.stopName = navigationResponse.stopName
        navigationResponse.trackerEventType = navigationResponse.trackerEventType
        navigationResponse.coordinates?.let { Assert.assertTrue(TEST_FAILED_NAVIGATION_DATA, it.isEmpty()) }
    }

    @Test
    fun simulationHistoryResponseTest() {
        val navigationResponse = SimulationHistoryData(
            headerData = "test",
            isBusStopData = false,
            busStopCount = 0,
            devicePositionData = SimulationHistoryInnerData(
                latitude = 0.0,
                longitude = 0.0,
                receivedTime = Date()
            )
        )
        navigationResponse.headerData = navigationResponse.headerData
        navigationResponse.isBusStopData = navigationResponse.isBusStopData
        navigationResponse.busStopCount = navigationResponse.busStopCount
        navigationResponse.devicePositionData = navigationResponse.devicePositionData
        Assert.assertTrue(TEST_FAILED_NAVIGATION_DATA, navigationResponse.headerData == "test")
    }

    @Test
    fun busRouteResponseTest() {
        val navigationResponse = BusRouteCoordinates(
            id = "1",
            geofenceCollection = "test",
            coordinates = listOf(),
            isUpdateNeeded = false
        )
        navigationResponse.id = navigationResponse.id
        navigationResponse.geofenceCollection = navigationResponse.geofenceCollection
        navigationResponse.coordinates = navigationResponse.coordinates
        navigationResponse.isUpdateNeeded = navigationResponse.isUpdateNeeded
        Assert.assertTrue(TEST_FAILED_NAVIGATION_DATA, navigationResponse.id == "1")
    }
}
