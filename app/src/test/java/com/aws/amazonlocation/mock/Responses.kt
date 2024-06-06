package com.aws.amazonlocation.mock

import com.amazonaws.services.geo.model.BatchDeleteDevicePositionHistoryResult
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.CalculateRouteSummary
import com.amazonaws.services.geo.model.Circle
import com.amazonaws.services.geo.model.DevicePosition
import com.amazonaws.services.geo.model.DistanceUnit
import com.amazonaws.services.geo.model.GeofenceGeometry
import com.amazonaws.services.geo.model.GetDevicePositionHistoryResult
import com.amazonaws.services.geo.model.Leg
import com.amazonaws.services.geo.model.LegGeometry
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.amazonaws.services.geo.model.Place
import com.amazonaws.services.geo.model.PlaceGeometry
import com.amazonaws.services.geo.model.SearchForPositionResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionSummary
import com.amazonaws.services.geo.model.Step
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.models.Coordinates
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import java.util.Date

object Responses {

    val RESPONSE_SEARCH_TEXT_RIO_TINTO = SearchSuggestionResponse(
        text = "rio tinto",
        maxResults = 5,
        language = "en",
        dataSource = ESRI,
        data = arrayListOf(
            SearchSuggestionData(
                placeId = "AQAAAIAACg_vXCRfx8HI5rtgk0Mmnd6PPxpnkNiPXtNOQK-wtPFoIHEB39lZ9bAxMIy5uXFWjHYaOiaMYcI38eByA0ztmt4BmQMA3CvJArxga_45fdoDEIJWlnAfeXv9ExWR1Z32Bwo6K4xtfdSN3EgYLLwjiUEnbyUjTOJdJuy_iwZ8O1p_uCOJDEvvzA5WYaEkFFmGXzTcMxZuYRyKpxlC6EPvWA",
                searchText = "rio tinto",
                text = "Rio Tinto, Gondomar, Porto, PRT",
                distance = 8996050.0,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(41.17271000000005, -8.556649999999934),
                    label = "Rio Tinto, Gondomar, Porto, PRT",
                    addressNumber = null,
                    street = null,
                    country = "PRT",
                    region = "Porto",
                    subRegion = "Gondomar",
                    municipality = "Gondomar",
                    neighborhood = "Rio Tinto",
                    postalCode = null
                )
            ),
            SearchSuggestionData(
                placeId = "AQAAAIAA3iDmWtWuLicpbqS5M2Mx9Tu7cuOpnFlUvEAyG5bCiAUGHir-hC7W-WjEtmp2wanCf9N1tb0ucKUfTzGyi6OvHIeZHoRI9HkbSBdn-9ZciqUZOTL-c0RIRY4dAYh_V_HtdILotOYkVMLyS7oji7kb263UkGchZeBEs5Zd_Xa3xGJiqnNTsnCDEiIOlOEd3FcBgk8kyXbmj6nRYZylfZ_p7g",
                searchText = "rio tinto",
                text = "Rio Tinto, Paraiba, BRA",
                distance = 1.0205287E7,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(-6.811359999999979, -35.07542999999998),
                    label = "Rio Tinto, Paraiba, BRA",
                    addressNumber = null,
                    street = null,
                    country = "BRA",
                    region = "Paraiba",
                    subRegion = null,
                    municipality = "Rio Tinto",
                    neighborhood = null,
                    postalCode = null
                )
            ),
            SearchSuggestionData(
                placeId = "AQAAAIAAou9Y0RuRsQLJHjkLYIPmxsL5eomLtjMTV77WmN9Kfqj4OaOGLrEeRX7_Vx5JJTXcRuvUQ1Mhm1xrfiXquEPkklYqRQYju-bjuKkUhINJ__xlmV5kguwifl0i3sc4LhUuyzzh_zNZbIROonF7eSeh98fIdklhNXnfH42u8UdkR2NYStBxd0nBDpNf7Sn6U8vp7SJ-a90A8eChHlyaB0PqWA",
                searchText = "rio tinto",
                text = "Rio Tinto, Esposende, Braga, PRT",
                distance = 8961604.0,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(41.49918000000008, -8.718289999999968),
                    label = "Rio Tinto, Esposende, Braga, PRT",
                    addressNumber = null,
                    street = null,
                    country = "PRT",
                    region = "Braga",
                    subRegion = "Esposende",
                    municipality = "Esposende",
                    neighborhood = "Rio Tinto",
                    postalCode = null
                )
            ),
            SearchSuggestionData(
                placeId = "AQAAAIAATDjvgdm8fMuOOTL4HjSgv90Oqll6po6NbNi8hRyf6Yq7BlxjBs_d0kC17taZ7uB9oy9ZU1zZlq-wFSDSxbW-N4x2vetQsKWyXKMmpK4P0psuyXse1DF3yQ-fG89rKTbv-BKVmkW1hbzS1-3YMKSHil0_4k2R-IrRQ3rUQp8SyFrkS46NGAk4Vb0AeQw3WRGcPxpC4BlQg_6Uap2xNlrDoA",
                searchText = "rio tinto",
                text = "Rio Tinto, Amares, Braga, PRT",
                distance = 8971593.0,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(41.62606000000005, -8.401579999999967),
                    label = "Rio Tinto, Amares, Braga, PRT",
                    addressNumber = null,
                    street = null,
                    country = "PRT",
                    region = "Braga",
                    subRegion = "Amares",
                    municipality = "Amares",
                    neighborhood = "Rio Tinto",
                    postalCode = null
                )
            ),
            SearchSuggestionData(
                placeId = "AQAAAIAAri9JPWCRlhRHXk7ZzGj8jZj3DT1OKUkNYCIl6SrSdZLpIeufCS1hhhvvnyQS9xc50ZGUvS3-Z-3GZLKzx6YAbrla6nGMwziPkligVgYAtGrJ4n9W2rTrRw3pLzdrh-Bns04nwIVR3CFvVIZu4m6kHIBENAIV_ApVFA2U9smvriVDR2lygNkdYXlUZ0gvwgMyXcnEJXmyakTuRmSvuj71Og",
                searchText = "rio tinto",
                text = "Rio Tinto, Vagos, Aveiro, PRT",
                distance = 9041692.0,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(40.488560000000064, -8.64805999999993),
                    label = "Rio Tinto, Vagos, Aveiro, PRT",
                    addressNumber = null,
                    street = null,
                    country = "PRT",
                    region = "Aveiro",
                    subRegion = "Vagos",
                    municipality = "Vagos",
                    neighborhood = "Rio Tinto",
                    postalCode = null
                )
            )
        ),
        error = null
    )

    val RESPONSE_PLACE_INDEX_RIO_TINTO = SearchSuggestionResponse(
        text = "Rio Tinto",
        maxResults = 15,
        language = "en",
        dataSource = ESRI,
        data = arrayListOf(
            SearchSuggestionData(
                placeId = null,
                searchText = "Rio Tinto",
                text = "Rio Tinto, Gondomar, Porto, PRT",
                distance = 8996050.0,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(41.17271000000005, -8.556649999999934),
                    label = "Rio Tinto, Gondomar, Porto, PRT",
                    addressNumber = null,
                    street = null,
                    country = "PRT",
                    region = "Porto",
                    subRegion = "Gondomar",
                    municipality = "Gondomar",
                    neighborhood = "Rio Tinto",
                    postalCode = null
                )
            ),
            SearchSuggestionData(
                placeId = null,
                searchText = "Rio Tinto",
                text = "Rio Tinto, Paraíba, BRA",
                distance = 1.0205287E7,
                isDestination = false,
                isPlaceIndexForPosition = false,
                amazonLocationPlace = AmazonLocationPlace(
                    coordinates = Coordinates(-6.811359999999979, -35.07542999999998),
                    label = "Rio Tinto, Paraíba, BRA",
                    addressNumber = null,
                    street = null,
                    country = "BRA",
                    region = "Paraíba",
                    subRegion = null,
                    municipality = "Rio Tinto",
                    neighborhood = null,
                    postalCode = null
                )
            )
        ),
        error = null
    )

    var RESPONSE_PLACE_INDEX_RIO_TINTO_ERROR = SearchSuggestionResponse(
        error = API_ERROR
    )

    val RESPONSE_CALCULATE_DISTANCE_CAR = CalculateRouteResult()
        .withLegs(
            Leg()
                .withDistance(0.19504914469655174)
                .withDurationSeconds(49.556657883)
                .withEndPosition(72.83338298024672, 18.921575535610945)
                .withGeometry(
                    LegGeometry()
                        .withLineString(
                            listOf(72.83371801248408, 18.922163986834548),
                            listOf(72.83368999799727, 18.922080011205793),
                            listOf(72.83332001005944, 18.92124998876538),
                            listOf(72.83318000545705, 18.92106999738339),
                            listOf(72.83317999485907, 18.92106998837811),
                            listOf(72.83324999683737, 18.921269989185305),
                            listOf(72.83338298024672, 18.921575535610945)
                        )
                )
                .withStartPosition(72.83371801248408, 18.922163986834548)
                .withSteps(
                    Step()
                        .withDistance(0.1349945068479063)
                        .withDurationSeconds(32.851806643)
                        .withEndPosition(72.83318000545705, 18.92106999738339)
                        .withGeometryOffset(1)
                        .withStartPosition(72.83368999799727, 18.922080011205793),
                    Step()
                        .withDistance(0.060054637848645454)
                        .withDurationSeconds(16.70485124)
                        .withEndPosition(72.83338298024672, 18.921575535610945)
                        .withGeometryOffset(4)
                        .withStartPosition(72.83317999485907, 18.921069988378113)
                )
        )
        .withSummary(
            CalculateRouteSummary()
                .withDataSource(ESRI)
                .withDistance(0.19504914469655174)
                .withDistanceUnit(DistanceUnit.Kilometers)
                .withDurationSeconds(49.556657883)
                .withRouteBBox(
                    72.83317999485907,
                    18.92106998837811,
                    72.83371801248408,
                    18.922163986834548
                )
        )

    val RESPONSE_CALCULATE_DISTANCE_BICYCLE = CalculateRouteResult()
        .withLegs(
            Leg()
                .withDistance(0.19504914469655174)
                .withDurationSeconds(140.439713998)
                .withEndPosition(72.83338298024672, 18.921575535610945)
                .withGeometry(
                    LegGeometry()
                        .withLineString(
                            listOf(72.83371801248408, 18.922163986834548),
                            listOf(72.83368999799727, 18.922080011205793),
                            listOf(72.83332001005944, 18.92124998876538),
                            listOf(72.83326001293212, 18.92112999451072),
                            listOf(72.83318000545705, 18.92106999738339),
                            listOf(72.83317999485907, 18.92106998837811),
                            listOf(72.83324999683737, 18.921269989185305),
                            listOf(72.83338298024672, 18.921575535610945)
                        )
                )
                .withStartPosition(72.83371801248408, 18.922163986834548)
                .withSteps(
                    Step()
                        .withDistance(0.1349945068479063)
                        .withDurationSeconds(97.199218758)
                        .withEndPosition(72.83318000545705, 18.92106999738339)
                        .withGeometryOffset(1)
                        .withStartPosition(72.83368999799727, 18.922080011205793),
                    Step()
                        .withDistance(0.060054637848645454)
                        .withDurationSeconds(43.24049524)
                        .withEndPosition(72.83338298024672, 18.921575535610945)
                        .withGeometryOffset(5)
                        .withStartPosition(72.83317999485907, 18.92106998837811)
                )
        )
        .withSummary(
            CalculateRouteSummary()
                .withDataSource(GRAB)
                .withDistance(0.19504914469655174)
                .withDistanceUnit(DistanceUnit.Kilometers)
                .withDurationSeconds(140.439713998)
                .withRouteBBox(
                    72.83317999485907,
                    18.92106998837811,
                    72.83371801248408,
                    18.922163986834548
                )
        )

    val RESPONSE_CALCULATE_DISTANCE_MOTORCYCLE = CalculateRouteResult()
        .withLegs(
            Leg()
                .withDistance(0.19504914469655174)
                .withDurationSeconds(141.439713998)
                .withEndPosition(72.83338298024672, 18.921575535610945)
                .withGeometry(
                    LegGeometry()
                        .withLineString(
                            listOf(72.83371801248408, 18.922163986834548),
                            listOf(72.83368999799727, 18.922080011205793),
                            listOf(72.83332001005944, 18.92124998876538),
                            listOf(72.83326001293212, 18.92112999451072),
                            listOf(72.83318000545705, 18.92106999738339),
                            listOf(72.83317999485907, 18.92106998837811),
                            listOf(72.83324999683737, 18.921269989185305),
                            listOf(72.83338298024672, 18.921575535610945)
                        )
                )
                .withStartPosition(72.83371801248408, 18.922163986834548)
                .withSteps(
                    Step()
                        .withDistance(0.1349945068479063)
                        .withDurationSeconds(97.199218758)
                        .withEndPosition(72.83318000545705, 18.92106999738339)
                        .withGeometryOffset(1)
                        .withStartPosition(72.83368999799727, 18.922080011205793),
                    Step()
                        .withDistance(0.060054637848645454)
                        .withDurationSeconds(43.24049524)
                        .withEndPosition(72.83338298024672, 18.921575535610945)
                        .withGeometryOffset(5)
                        .withStartPosition(72.83317999485907, 18.92106998837811)
                )
        )
        .withSummary(
            CalculateRouteSummary()
                .withDataSource(GRAB)
                .withDistance(0.19504914469655174)
                .withDistanceUnit(DistanceUnit.Kilometers)
                .withDurationSeconds(140.439713998)
                .withRouteBBox(
                    72.83317999485907,
                    18.92106998837811,
                    72.83371801248408,
                    18.922163986834548
                )
        )

    val RESPONSE_CALCULATE_DISTANCE_WALKING = CalculateRouteResult()
        .withLegs(
            Leg()
                .withDistance(0.19504914469655174)
                .withDurationSeconds(140.439713998)
                .withEndPosition(72.83338298024672, 18.921575535610945)
                .withGeometry(
                    LegGeometry()
                        .withLineString(
                            listOf(72.83371801248408, 18.922163986834548),
                            listOf(72.83368999799727, 18.922080011205793),
                            listOf(72.83332001005944, 18.92124998876538),
                            listOf(72.83326001293212, 18.92112999451072),
                            listOf(72.83318000545705, 18.92106999738339),
                            listOf(72.83317999485907, 18.92106998837811),
                            listOf(72.83324999683737, 18.921269989185305),
                            listOf(72.83338298024672, 18.921575535610945)
                        )
                )
                .withStartPosition(72.83371801248408, 18.922163986834548)
                .withSteps(
                    Step()
                        .withDistance(0.1349945068479063)
                        .withDurationSeconds(97.199218758)
                        .withEndPosition(72.83318000545705, 18.92106999738339)
                        .withGeometryOffset(1)
                        .withStartPosition(72.83368999799727, 18.922080011205793),
                    Step()
                        .withDistance(0.060054637848645454)
                        .withDurationSeconds(43.24049524)
                        .withEndPosition(72.83338298024672, 18.921575535610945)
                        .withGeometryOffset(5)
                        .withStartPosition(72.83317999485907, 18.92106998837811)
                )
        )
        .withSummary(
            CalculateRouteSummary()
                .withDataSource(ESRI)
                .withDistance(0.19504914469655174)
                .withDistanceUnit(DistanceUnit.Kilometers)
                .withDurationSeconds(140.439713998)
                .withRouteBBox(
                    72.83317999485907,
                    18.92106998837811,
                    72.83371801248408,
                    18.922163986834548
                )
        )

    val RESPONSE_CALCULATE_DISTANCE_TRUCK = CalculateRouteResult()
        .withLegs(
            Leg()
                .withDistance(0.19504914469655174)
                .withDurationSeconds(49.556657883)
                .withEndPosition(72.83338298024672, 18.921575535610945)
                .withGeometry(
                    LegGeometry()
                        .withLineString(
                            listOf(72.83371801248408, 18.922163986834548),
                            listOf(72.83368999799727, 18.922080011205793),
                            listOf(72.83332001005944, 18.92124998876538),
                            listOf(72.83318000545705, 18.92106999738339),
                            listOf(72.83317999485907, 18.92106998837811),
                            listOf(72.83324999683737, 18.921269989185305),
                            listOf(72.83338298024672, 18.921575535610945)
                        )
                )
                .withStartPosition(72.83371801248408, 18.922163986834548)
                .withSteps(
                    Step()
                        .withDistance(0.1349945068479063)
                        .withDurationSeconds(32.851806643)
                        .withEndPosition(72.83318000545705, 18.92106999738339)
                        .withGeometryOffset(1)
                        .withStartPosition(72.83368999799727, 18.922080011205793),
                    Step()
                        .withDistance(0.060054637848645454)
                        .withDurationSeconds(16.70485124)
                        .withEndPosition(72.83338298024672, 18.921575535610945)
                        .withGeometryOffset(4)
                        .withStartPosition(72.83317999485907, 18.92106998837811)
                )
        )
        .withSummary(
            CalculateRouteSummary()
                .withDataSource(ESRI)
                .withDistance(0.19504914469655174)
                .withDistanceUnit(DistanceUnit.Kilometers)
                .withDurationSeconds(49.556657883)
                .withRouteBBox(
                    72.83317999485907,
                    18.92106998837811,
                    72.83371801248408,
                    18.922163986834548
                )
        )

    val RESPONSE_NAVIGATION_DATA_CAR_STEP_1 = NavigationData(
        duration = 32.851806643,
        distance = 0.1349945068479063,
        startLat = 72.83368999799727,
        startLng = 18.922080011205793,
        endLat = 72.83318000545705,
        endLng = 18.92106999738339,
        destinationAddress = "Prem Ramchandani Marg, Colaba, Mumbai, Maharashtra, 400005, IND",
        region = "Maharashtra",
        subRegion = "Mumbai",
        country = "IND"
    )

    val RESPONSE_NAVIGATION_DATA_CAR_STEP_2 = NavigationData(
        duration = 16.70485124,
        distance = 0.060054637848645454,
        startLat = 72.83317999485907,
        startLng = 18.92106998837811,
        endLat = 72.83338298024672,
        endLng = 18.921575535610945,
        destinationAddress = "Jokim Alva Chowk, Colaba, Mumbai, Maharashtra, 400005, IND",
        region = "Maharashtra",
        subRegion = "Mumbai",
        country = "IND"
    )

    val RESPONSE_GEOFENCE_LIST = GeofenceData(
        geofenceList = arrayListOf(
            ListGeofenceResponseEntry().withCreateTime(Date())
                .withGeofenceId("fdf")
                .withStatus(TEST_DATA_7)
                .withUpdateTime(Date())
                .withGeometry(
                    GeofenceGeometry().withCircle(
                        Circle().withCenter(
                            TEST_DATA_LAT_1,
                            TEST_DATA_LNG_1
                        )
                    )
                ),
            ListGeofenceResponseEntry().withCreateTime(Date())
                .withGeofenceId("gg")
                .withStatus(TEST_DATA_7)
                .withUpdateTime(Date())
                .withGeometry(
                    GeofenceGeometry().withCircle(
                        Circle().withCenter(
                            TEST_DATA_LAT_1,
                            TEST_DATA_LNG_1
                        )
                    )
                )
        ),
        message = null
    )

    val ERROR_RESPONSE_GEOFENCE_LIST = GeofenceData(
        message = NO_DATA_FOUND
    )

    val SEARCH_RESPONSE_TAJ = SearchResponse(
        searchPlaceIndexForPositionResult = SearchPlaceIndexForPositionResult()
            .withResults(
                SearchForPositionResult()
                    .withDistance(0.0)
                    .withPlace(
                        Place()
                            .withCountry("IND")
                            .withGeometry(
                                PlaceGeometry()
                                    .withPoint(TEST_DATA_LAT, TEST_DATA_LNG)
                            )
                            .withInterpolated(false)
                            .withLabel("The Taj, Mumbai, Mahārāshtra, IND")
                            .withRegion("Mahārāshtra")
                            .withSubRegion("Mumbai")
                    )
            )
            .withSummary(
                SearchPlaceIndexForPositionSummary()
                    .withDataSource(ESRI)
                    .withLanguage("en")
                    .withMaxResults(1)
                    .withPosition(TEST_DATA_LAT, TEST_DATA_LNG)
            ),
        latitude = TEST_DATA_LNG,
        longitude = TEST_DATA_LAT
    )

    val RESPONSE_ADDRESS_LINE_FROM_LAT_LNG = SearchPlaceIndexForPositionResult()
        .withResults(
            SearchForPositionResult()
                .withDistance(0.0)
                .withPlace(
                    Place()
                        .withCountry("IND")
                        .withGeometry(
                            PlaceGeometry()
                                .withPoint(72.83468000000005, 18.921880000000044)
                        )
                        .withInterpolated(false)
                        .withLabel("Gateway of India, Mumbai, Mahārāshtra, IND")
                        .withRegion("Mahārāshtra")
                        .withSubRegion("Mumbai")
                )
        )
        .withSummary(
            SearchPlaceIndexForPositionSummary()
                .withDataSource(ESRI)
                .withLanguage("en")
                .withMaxResults(1)
                .withPosition(72.83468000000005, 18.921880000000044)
        )

    val RESPONSE_LOCATION_HISTORY = LocationHistoryResponse(
        errorMessage = null,
        response = GetDevicePositionHistoryResult()
            .withDevicePositions(
                DevicePosition()
                    .withDeviceId("662f86eddc909886")
                    .withPosition(72.83373, 18.92216)
                    .withPositionProperties(
                        mapOf(
                            "id" to "c83280ad-a374-4dee-870c-ef695daf2bc1",
                            "region" to "us-east-1"
                        )
                    )
                    .withReceivedTime(Date(2023, 4, 14, 12, 22, 17))
                    .withSampleTime(Date(2023, 4, 14, 12, 22, 15))
            )
    )

    val RESPONSE_LOCATION_HISTORY_TODAY = LocationHistoryResponse(
        errorMessage = null,
        response = GetDevicePositionHistoryResult()
            .withDevicePositions(
                DevicePosition()
                    .withDeviceId("662f86eddc909886")
                    .withPosition(72.83373, 18.92216)
                    .withPositionProperties(
                        mapOf(
                            "id" to "c83280ad-a374-4dee-870c-ef695daf2bc1",
                            "region" to "us-east-1"
                        )
                    )
                    .withReceivedTime(Date(2023, 4, 14, 12, 22, 17))
                    .withSampleTime(Date(2023, 4, 14, 12, 22, 15))
            )
    )

    val RESPONSE_ERROR_LOCATION_HISTORY = LocationHistoryResponse(
        errorMessage = MOCK_ERROR,
        response = null
    )

    val RESPONSE_ERROR_LOCATION_HISTORY_NO_ERROR = LocationHistoryResponse(
        errorMessage = null,
        response = null
    )

    val RESPONSE_DELETE_TRACKING_HISTORY = DeleteLocationHistoryResponse(
        errorMessage = null,
        response = BatchDeleteDevicePositionHistoryResult()
            .withErrors()
    )

    val RESPONSE_ERROR_DELETE_TRACKING_HISTORY = DeleteLocationHistoryResponse(
        errorMessage = MOCK_ERROR,
        response = null
    )

    val RESPONSE_ERROR_DELETE_TRACKING_HISTORY_NO_ERROR = DeleteLocationHistoryResponse(
        errorMessage = null,
        response = null
    )

    val RESPONSE_TRACKER_GEOFENCE_LIST = GeofenceData(
        message = null,
        geofenceList = arrayListOf(
            ListGeofenceResponseEntry()
                .withGeofenceId("bb")
                .withStatus(TEST_DATA_7)
                .withCreateTime(Date(2023, 4, 14, 13, 13, 54))
                .withUpdateTime(Date(2023, 4, 14, 13, 13, 54))
                .withGeometry(
                    GeofenceGeometry()
                        .withCircle(
                            Circle()
                                .withCenter(72.530537125, 23.0147063)
                                .withRadius(778.0)
                        )
                )
        )
    )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST = GeofenceData(
        message = MOCK_ERROR,
        geofenceList = arrayListOf()
    )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST_NO_ERROR = GeofenceData(
        message = null,
        geofenceList = arrayListOf()
    )

    val RESPONSE_SIGN_IN = LoginResponse(
        name = BuildConfig.USER_LOGIN_NAME + " TEST",
        email = null,
        success = SIGN_IN_SUCCESS,
        provider = "cognito-idp.us-east-1.amazonaws.com/us-east-1_48VeDo2Uw",
        idToken = "Mock Token"
    )
}
