package com.aws.amazonlocation.mock

import aws.sdk.kotlin.services.location.model.BatchDeleteDevicePositionHistoryResponse
import aws.sdk.kotlin.services.location.model.CalculateRouteResponse
import aws.sdk.kotlin.services.location.model.CalculateRouteSummary
import aws.sdk.kotlin.services.location.model.Circle
import aws.sdk.kotlin.services.location.model.DevicePosition
import aws.sdk.kotlin.services.location.model.DistanceUnit
import aws.sdk.kotlin.services.location.model.GeofenceGeometry
import aws.sdk.kotlin.services.location.model.GetDevicePositionHistoryResponse
import aws.sdk.kotlin.services.location.model.Leg
import aws.sdk.kotlin.services.location.model.LegGeometry
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.sdk.kotlin.services.location.model.Place
import aws.sdk.kotlin.services.location.model.PlaceGeometry
import aws.sdk.kotlin.services.location.model.SearchForPositionResult
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionSummary
import aws.sdk.kotlin.services.location.model.Step
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import java.util.Date
import java.util.GregorianCalendar

object Responses {
    val RESPONSE_SEARCH_TEXT_RIO_TINTO =
        SearchSuggestionResponse(
            text = "rio tinto",
            maxResults = 5,
            language = "en",
            dataSource = ESRI,
            data =
                arrayListOf(
                    SearchSuggestionData(
                        placeId = "AQAAAIAACg_vXCRfx8HI5rtgk0Mmnd6PPxpnkNiPXtNOQK-wtPFoIHEB39lZ9bAxMIy5uXFWjHYaOiaMYcI38eByA0ztmt4BmQMA3CvJArxga_45fdoDEIJWlnAfeXv9ExWR1Z32Bwo6K4xtfdSN3EgYLLwjiUEnbyUjTOJdJuy_iwZ8O1p_uCOJDEvvzA5WYaEkFFmGXzTcMxZuYRyKpxlC6EPvWA",
                        searchText = "rio tinto",
                        text = "Rio Tinto, Gondomar, Porto, PRT",
                        distance = 8996050.0,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                    SearchSuggestionData(
                        placeId = "AQAAAIAA3iDmWtWuLicpbqS5M2Mx9Tu7cuOpnFlUvEAyG5bCiAUGHir-hC7W-WjEtmp2wanCf9N1tb0ucKUfTzGyi6OvHIeZHoRI9HkbSBdn-9ZciqUZOTL-c0RIRY4dAYh_V_HtdILotOYkVMLyS7oji7kb263UkGchZeBEs5Zd_Xa3xGJiqnNTsnCDEiIOlOEd3FcBgk8kyXbmj6nRYZylfZ_p7g",
                        searchText = "rio tinto",
                        text = "Rio Tinto, Paraiba, BRA",
                        distance = 1.0205287E7,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                    SearchSuggestionData(
                        placeId = "AQAAAIAAou9Y0RuRsQLJHjkLYIPmxsL5eomLtjMTV77WmN9Kfqj4OaOGLrEeRX7_Vx5JJTXcRuvUQ1Mhm1xrfiXquEPkklYqRQYju-bjuKkUhINJ__xlmV5kguwifl0i3sc4LhUuyzzh_zNZbIROonF7eSeh98fIdklhNXnfH42u8UdkR2NYStBxd0nBDpNf7Sn6U8vp7SJ-a90A8eChHlyaB0PqWA",
                        searchText = "rio tinto",
                        text = "Rio Tinto, Esposende, Braga, PRT",
                        distance = 8961604.0,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                    SearchSuggestionData(
                        placeId = "AQAAAIAATDjvgdm8fMuOOTL4HjSgv90Oqll6po6NbNi8hRyf6Yq7BlxjBs_d0kC17taZ7uB9oy9ZU1zZlq-wFSDSxbW-N4x2vetQsKWyXKMmpK4P0psuyXse1DF3yQ-fG89rKTbv-BKVmkW1hbzS1-3YMKSHil0_4k2R-IrRQ3rUQp8SyFrkS46NGAk4Vb0AeQw3WRGcPxpC4BlQg_6Uap2xNlrDoA",
                        searchText = "rio tinto",
                        text = "Rio Tinto, Amares, Braga, PRT",
                        distance = 8971593.0,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                    SearchSuggestionData(
                        placeId = "AQAAAIAAri9JPWCRlhRHXk7ZzGj8jZj3DT1OKUkNYCIl6SrSdZLpIeufCS1hhhvvnyQS9xc50ZGUvS3-Z-3GZLKzx6YAbrla6nGMwziPkligVgYAtGrJ4n9W2rTrRw3pLzdrh-Bns04nwIVR3CFvVIZu4m6kHIBENAIV_ApVFA2U9smvriVDR2lygNkdYXlUZ0gvwgMyXcnEJXmyakTuRmSvuj71Og",
                        searchText = "rio tinto",
                        text = "Rio Tinto, Vagos, Aveiro, PRT",
                        distance = 9041692.0,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                ),
            error = null,
        )

    val RESPONSE_PLACE_INDEX_RIO_TINTO =
        SearchSuggestionResponse(
            text = "Rio Tinto",
            maxResults = 15,
            language = "en",
            dataSource = ESRI,
            data =
                arrayListOf(
                    SearchSuggestionData(
                        placeId = null,
                        searchText = "Rio Tinto",
                        text = "Rio Tinto, Gondomar, Porto, PRT",
                        distance = 8996050.0,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                    SearchSuggestionData(
                        placeId = null,
                        searchText = "Rio Tinto",
                        text = "Rio Tinto, Paraíba, BRA",
                        distance = 1.0205287E7,
                        isDestination = false,
                        isPlaceIndexForPosition = false,
                    ),
                ),
            error = null,
        )

    var RESPONSE_PLACE_INDEX_RIO_TINTO_ERROR =
        SearchSuggestionResponse(
            error = API_ERROR,
        )

    val RESPONSE_CALCULATE_DISTANCE_CAR =
        CalculateRouteResponse {
            legs =
                listOf(
                    Leg {
                        distance = 0.19504914469655174
                        durationSeconds = 49.556657883
                        endPosition = listOf(72.83338298024672, 18.921575535610945)
                        geometry =
                            LegGeometry {
                                lineString =
                                    listOf(
                                        listOf(72.83371801248408, 18.922163986834548),
                                        listOf(72.83368999799727, 18.922080011205793),
                                        listOf(72.83332001005944, 18.92124998876538),
                                        listOf(72.83318000545705, 18.92106999738339),
                                        listOf(72.83317999485907, 18.92106998837811),
                                        listOf(72.83324999683737, 18.921269989185305),
                                        listOf(72.83338298024672, 18.921575535610945),
                                    )
                            }
                        startPosition = listOf(72.83371801248408, 18.922163986834548)
                        steps =
                            listOf(
                                Step {
                                    distance = 0.1349945068479063
                                    durationSeconds = 32.851806643
                                    endPosition = listOf(72.83318000545705, 18.92106999738339)
                                    geometryOffset = 1
                                    startPosition = listOf(72.83368999799727, 18.922080011205793)
                                },
                                Step {
                                    distance = 0.060054637848645454
                                    durationSeconds = 16.70485124
                                    endPosition = listOf(72.83338298024672, 18.921575535610945)
                                    geometryOffset = 4
                                    startPosition = listOf(72.83317999485907, 18.921069988378113)
                                },
                            )
                    },
                )
            summary =
                CalculateRouteSummary {
                    dataSource = ESRI
                    distance = 0.19504914469655174
                    distanceUnit = DistanceUnit.Kilometers
                    durationSeconds = 49.556657883
                    routeBBox =
                        listOf(
                            72.83317999485907,
                            18.92106998837811,
                            72.83371801248408,
                            18.922163986834548,
                        )
                }
        }

    val RESPONSE_CALCULATE_DISTANCE_BICYCLE =
        CalculateRouteResponse {
            legs =
                listOf(
                    Leg {
                        distance = 0.19504914469655174
                        durationSeconds = 140.439713998
                        endPosition = listOf(72.83338298024672, 18.921575535610945)
                        geometry =
                            LegGeometry {
                                lineString =
                                    listOf(
                                        listOf(72.83371801248408, 18.922163986834548),
                                        listOf(72.83368999799727, 18.922080011205793),
                                        listOf(72.83332001005944, 18.92124998876538),
                                        listOf(72.83326001293212, 18.92112999451072),
                                        listOf(72.83318000545705, 18.92106999738339),
                                        listOf(72.83317999485907, 18.92106998837811),
                                        listOf(72.83324999683737, 18.921269989185305),
                                        listOf(72.83338298024672, 18.921575535610945),
                                    )
                            }
                        startPosition = listOf(72.83371801248408, 18.922163986834548)
                        steps =
                            listOf(
                                Step {
                                    distance = 0.1349945068479063
                                    durationSeconds = 97.199218758
                                    endPosition = listOf(72.83318000545705, 18.92106999738339)
                                    geometryOffset = 1
                                    startPosition = listOf(72.83368999799727, 18.922080011205793)
                                },
                                Step {
                                    distance = 0.060054637848645454
                                    durationSeconds = 43.24049524
                                    endPosition = listOf(72.83338298024672, 18.921575535610945)
                                    geometryOffset = 5
                                    startPosition = listOf(72.83317999485907, 18.92106998837811)
                                },
                            )
                    },
                )
            summary =
                CalculateRouteSummary {
                    dataSource = GRAB
                    distance = 0.19504914469655174
                    distanceUnit = DistanceUnit.Kilometers
                    durationSeconds = 140.439713998
                    routeBBox =
                        listOf(
                            72.83317999485907,
                            18.92106998837811,
                            72.83371801248408,
                            18.922163986834548,
                        )
                }
        }

    val RESPONSE_CALCULATE_DISTANCE_MOTORCYCLE =
        CalculateRouteResponse {
            legs =
                listOf(
                    Leg {
                        distance = 0.19504914469655174
                        durationSeconds = 141.439713998
                        endPosition = listOf(72.83338298024672, 18.921575535610945)
                        geometry =
                            LegGeometry {
                                lineString =
                                    listOf(
                                        listOf(72.83371801248408, 18.922163986834548),
                                        listOf(72.83368999799727, 18.922080011205793),
                                        listOf(72.83332001005944, 18.92124998876538),
                                        listOf(72.83326001293212, 18.92112999451072),
                                        listOf(72.83318000545705, 18.92106999738339),
                                        listOf(72.83317999485907, 18.92106998837811),
                                        listOf(72.83324999683737, 18.921269989185305),
                                        listOf(72.83338298024672, 18.921575535610945),
                                    )
                            }
                        startPosition = listOf(72.83371801248408, 18.922163986834548)
                        steps =
                            listOf(
                                Step {
                                    distance = 0.1349945068479063
                                    durationSeconds = 97.199218758
                                    endPosition = listOf(72.83318000545705, 18.92106999738339)
                                    geometryOffset = 1
                                    startPosition = listOf(72.83368999799727, 18.922080011205793)
                                },
                                Step {
                                    distance = 0.060054637848645454
                                    durationSeconds = 43.24049524
                                    endPosition = listOf(72.83338298024672, 18.921575535610945)
                                    geometryOffset = 5
                                    startPosition = listOf(72.83317999485907, 18.92106998837811)
                                },
                            )
                    },
                )
            summary =
                CalculateRouteSummary {
                    dataSource = GRAB
                    distance = 0.19504914469655174
                    distanceUnit = DistanceUnit.Kilometers
                    durationSeconds = 141.439713998
                    routeBBox =
                        listOf(
                            72.83317999485907,
                            18.92106998837811,
                            72.83371801248408,
                            18.922163986834548,
                        )
                }
        }

    val RESPONSE_CALCULATE_DISTANCE_WALKING =
        CalculateRouteResponse {
            legs =
                listOf(
                    Leg {
                        distance = 0.19504914469655174
                        durationSeconds = 140.439713998
                        endPosition = listOf(72.83338298024672, 18.921575535610945)
                        geometry =
                            LegGeometry {
                                lineString =
                                    listOf(
                                        listOf(72.83371801248408, 18.922163986834548),
                                        listOf(72.83368999799727, 18.922080011205793),
                                        listOf(72.83332001005944, 18.92124998876538),
                                        listOf(72.83326001293212, 18.92112999451072),
                                        listOf(72.83318000545705, 18.92106999738339),
                                        listOf(72.83317999485907, 18.92106998837811),
                                        listOf(72.83324999683737, 18.921269989185305),
                                        listOf(72.83338298024672, 18.921575535610945),
                                    )
                            }
                        startPosition = listOf(72.83371801248408, 18.922163986834548)
                        steps =
                            listOf(
                                Step {
                                    distance = 0.1349945068479063
                                    durationSeconds = 97.199218758
                                    endPosition = listOf(72.83318000545705, 18.92106999738339)
                                    geometryOffset = 1
                                    startPosition = listOf(72.83368999799727, 18.922080011205793)
                                },
                                Step {
                                    distance = 0.060054637848645454
                                    durationSeconds = 43.24049524
                                    endPosition = listOf(72.83338298024672, 18.921575535610945)
                                    geometryOffset = 5
                                    startPosition = listOf(72.83317999485907, 18.92106998837811)
                                },
                            )
                    },
                )
            summary =
                CalculateRouteSummary {
                    dataSource = ESRI
                    distance = 0.19504914469655174
                    distanceUnit = DistanceUnit.Kilometers
                    durationSeconds = 140.439713998
                    routeBBox =
                        listOf(
                            72.83317999485907,
                            18.92106998837811,
                            72.83371801248408,
                            18.922163986834548,
                        )
                }
        }

    val RESPONSE_CALCULATE_DISTANCE_TRUCK =
        CalculateRouteResponse {
            legs =
                listOf(
                    Leg {
                        distance = 0.19504914469655174
                        durationSeconds = 49.556657883
                        endPosition = listOf(72.83338298024672, 18.921575535610945)
                        geometry =
                            LegGeometry {
                                lineString =
                                    listOf(
                                        listOf(72.83371801248408, 18.922163986834548),
                                        listOf(72.83368999799727, 18.922080011205793),
                                        listOf(72.83332001005944, 18.92124998876538),
                                        listOf(72.83318000545705, 18.92106999738339),
                                        listOf(72.83317999485907, 18.92106998837811),
                                        listOf(72.83324999683737, 18.921269989185305),
                                        listOf(72.83338298024672, 18.921575535610945),
                                    )
                            }
                        startPosition = listOf(72.83371801248408, 18.922163986834548)
                        steps =
                            listOf(
                                Step {
                                    distance = 0.1349945068479063
                                    durationSeconds = 32.851806643
                                    endPosition = listOf(72.83318000545705, 18.92106999738339)
                                    geometryOffset = 1
                                    startPosition = listOf(72.83368999799727, 18.922080011205793)
                                },
                                Step {
                                    distance = 0.060054637848645454
                                    durationSeconds = 16.70485124
                                    endPosition = listOf(72.83338298024672, 18.921575535610945)
                                    geometryOffset = 4
                                    startPosition = listOf(72.83317999485907, 18.92106998837811)
                                },
                            )
                    },
                )
            summary =
                CalculateRouteSummary {
                    dataSource = ESRI
                    distance = 0.19504914469655174
                    distanceUnit = DistanceUnit.Kilometers
                    durationSeconds = 49.556657883
                    routeBBox =
                        listOf(
                            72.83317999485907,
                            18.92106998837811,
                            72.83371801248408,
                            18.922163986834548,
                        )
                }
        }

    val RESPONSE_NAVIGATION_DATA_CAR_STEP_1 =
        NavigationData(
            duration = 32.851806643,
            distance = 0.1349945068479063,
            startLat = 72.83368999799727,
            startLng = 18.922080011205793,
            endLat = 72.83318000545705,
            endLng = 18.92106999738339,
            destinationAddress = "Prem Ramchandani Marg, Colaba, Mumbai, Maharashtra, 400005, IND",
            region = "Maharashtra",
            subRegion = "Mumbai",
            country = "IND",
        )

    val RESPONSE_NAVIGATION_DATA_CAR_STEP_2 =
        NavigationData(
            duration = 16.70485124,
            distance = 0.060054637848645454,
            startLat = 72.83317999485907,
            startLng = 18.92106998837811,
            endLat = 72.83338298024672,
            endLng = 18.921575535610945,
            destinationAddress = "Jokim Alva Chowk, Colaba, Mumbai, Maharashtra, 400005, IND",
            region = "Maharashtra",
            subRegion = "Mumbai",
            country = "IND",
        )

    val RESPONSE_GEOFENCE_LIST =
        GeofenceData(
            geofenceList =
                arrayListOf(
                    ListGeofenceResponseEntry {
                        createTime = Instant.now()
                        geofenceId = "fdf"
                        status = TEST_DATA_7
                        updateTime = Instant.now()
                        geometry =
                            GeofenceGeometry {
                                circle =
                                    Circle {
                                        center = listOf(TEST_DATA_LNG_1, TEST_DATA_LAT_1)
                                        radius = 100.00
                                    }
                            }
                    },
                    ListGeofenceResponseEntry {
                        createTime = Instant.now()
                        geofenceId = "gg"
                        status = TEST_DATA_7
                        updateTime = Instant.now()
                        geometry =
                            GeofenceGeometry {
                                circle =
                                    Circle {
                                        center = listOf(TEST_DATA_LNG_1, TEST_DATA_LAT_1)
                                        radius = 100.00
                                    }
                            }
                    },
                ),
            message = null,
        )

    val ERROR_RESPONSE_GEOFENCE_LIST =
        GeofenceData(
            message = NO_DATA_FOUND,
        )

    val SEARCH_RESPONSE_TAJ =
        SearchResponse(
            searchPlaceIndexForPositionResult = SearchPlaceIndexForPositionResponse {
                results = listOf(
                    SearchForPositionResult {
                        distance = 0.0
                        place = Place {
                            country = "IND"
                            geometry = PlaceGeometry {
                                point = listOf(TEST_DATA_LNG, TEST_DATA_LAT)
                            }
                            interpolated = false
                            label = "The Taj, Mumbai, Mahārāshtra, IND"
                            region = "Mahārāshtra"
                            subRegion = "Mumbai"
                        }
                    }
                )
                summary = SearchPlaceIndexForPositionSummary {
                    dataSource = "ESRI"
                    language = "en"
                    maxResults = 1
                    position = listOf(TEST_DATA_LNG, TEST_DATA_LAT)
                }
            },
            latitude = TEST_DATA_LAT,
            longitude = TEST_DATA_LNG,
        )

    val RESPONSE_ADDRESS_LINE_FROM_LAT_LNG =
        SearchPlaceIndexForPositionResponse {
            results = listOf(
                SearchForPositionResult {
                    distance = 0.0
                    place = Place {
                        country = "IND"
                        geometry = PlaceGeometry {
                            point = listOf(72.83468000000005, 18.921880000000044)
                        }
                        interpolated = false
                        label = "Gateway of India, Mumbai, Mahārāshtra, IND"
                        region = "Mahārāshtra"
                        subRegion = "Mumbai"
                    }
                }
            )
            summary = SearchPlaceIndexForPositionSummary {
                dataSource = "ESRI"
                language = "en"
                maxResults = 1
                position = listOf(72.83468000000005, 18.921880000000044)
            }
        }

    val RESPONSE_LOCATION_HISTORY =
        LocationHistoryResponse(
            errorMessage = null,
            response =
            GetDevicePositionHistoryResponse {
                devicePositions = listOf(
                    DevicePosition {
                        deviceId = "662f86eddc909886"
                        position = listOf(72.83373, 18.92216)
                        positionProperties = mapOf(
                            "id" to "c83280ad-a374-4dee-870c-ef695daf2bc1",
                            "region" to "us-east-1"
                        )
                        receivedTime = Instant.fromEpochMilliseconds(Date().time)
                        sampleTime = Instant.fromEpochMilliseconds(Date().time)
                    }
                )
            },
        )

    val RESPONSE_LOCATION_HISTORY_TODAY =
        LocationHistoryResponse(
            errorMessage = null,
            response =
            GetDevicePositionHistoryResponse {
                devicePositions = listOf(
                    DevicePosition {
                        deviceId = "662f86eddc909886"
                        position = listOf(72.83373, 18.92216)
                        positionProperties = mapOf(
                            "id" to "c83280ad-a374-4dee-870c-ef695daf2bc1",
                            "region" to "us-east-1"
                        )
                        receivedTime = Instant.fromEpochMilliseconds(Date().time)
                        sampleTime = Instant.fromEpochMilliseconds(Date().time)
                    }
                )
            },
        )

    val RESPONSE_ERROR_LOCATION_HISTORY =
        LocationHistoryResponse(
            errorMessage = MOCK_ERROR,
            response = null,
        )

    val RESPONSE_ERROR_LOCATION_HISTORY_NO_ERROR =
        LocationHistoryResponse(
            errorMessage = null,
            response = null,
        )

    val RESPONSE_DELETE_TRACKING_HISTORY =
        DeleteLocationHistoryResponse(
            errorMessage = null,
            response =
            BatchDeleteDevicePositionHistoryResponse {
                errors = listOf()
            },
        )

    val RESPONSE_ERROR_DELETE_TRACKING_HISTORY =
        DeleteLocationHistoryResponse(
            errorMessage = MOCK_ERROR,
            response = null,
        )

    val RESPONSE_ERROR_DELETE_TRACKING_HISTORY_NO_ERROR =
        DeleteLocationHistoryResponse(
            errorMessage = null,
            response = null,
        )

    val RESPONSE_TRACKER_GEOFENCE_LIST =
        GeofenceData(
            message = null,
            geofenceList =
                arrayListOf(
                    ListGeofenceResponseEntry {
                        geofenceId = "bb"
                        status = TEST_DATA_7
                        createTime = Instant.fromEpochMilliseconds(Date().time)
                        updateTime = Instant.fromEpochMilliseconds(Date().time)
                        geometry = GeofenceGeometry {
                            circle = Circle {
                                center = listOf(72.530537125, 23.0147063)
                                radius = 778.0
                            }
                        }
                    },
                ),
        )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST =
        GeofenceData(
            message = MOCK_ERROR,
            geofenceList = arrayListOf(),
        )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST_NO_ERROR =
        GeofenceData(
            message = null,
            geofenceList = arrayListOf(),
        )

    val RESPONSE_SIGN_IN =
        LoginResponse(
            name = BuildConfig.USER_LOGIN_NAME,
            email = null,
            success = SIGN_IN_SUCCESS,
            provider = "cognito-idp.us-east-1.amazonaws.com/us-east-1_48VeDo2Uw",
            idToken = "Mock Token",
        )
}
