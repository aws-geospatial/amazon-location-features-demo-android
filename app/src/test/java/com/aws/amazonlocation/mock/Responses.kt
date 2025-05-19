package com.aws.amazonlocation.mock

import aws.sdk.kotlin.services.geoplaces.model.Address
import aws.sdk.kotlin.services.geoplaces.model.ContactDetails
import aws.sdk.kotlin.services.geoplaces.model.Contacts
import aws.sdk.kotlin.services.geoplaces.model.Country
import aws.sdk.kotlin.services.geoplaces.model.GetPlaceResponse
import aws.sdk.kotlin.services.geoplaces.model.PlaceType
import aws.sdk.kotlin.services.geoplaces.model.Region
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse
import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResultItem
import aws.sdk.kotlin.services.geoplaces.model.SubRegion
import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import aws.sdk.kotlin.services.georoutes.model.GeometryFormat
import aws.sdk.kotlin.services.georoutes.model.LocalizedString
import aws.sdk.kotlin.services.georoutes.model.Route
import aws.sdk.kotlin.services.georoutes.model.RouteFerryLegDetails
import aws.sdk.kotlin.services.georoutes.model.RouteFerryNotice
import aws.sdk.kotlin.services.georoutes.model.RouteFerryNoticeCode
import aws.sdk.kotlin.services.georoutes.model.RouteFerrySummary
import aws.sdk.kotlin.services.georoutes.model.RouteFerryTravelStep
import aws.sdk.kotlin.services.georoutes.model.RouteFerryTravelStepType
import aws.sdk.kotlin.services.georoutes.model.RouteLeg
import aws.sdk.kotlin.services.georoutes.model.RouteLegGeometry
import aws.sdk.kotlin.services.georoutes.model.RouteLegTravelMode
import aws.sdk.kotlin.services.georoutes.model.RouteLegType
import aws.sdk.kotlin.services.georoutes.model.RouteMajorRoadLabel
import aws.sdk.kotlin.services.georoutes.model.RoutePassThroughPlace
import aws.sdk.kotlin.services.georoutes.model.RoutePassThroughWaypoint
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianLegDetails
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianNotice
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianNoticeCode
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianSummary
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianTravelStep
import aws.sdk.kotlin.services.georoutes.model.RoutePedestrianTravelStepType
import aws.sdk.kotlin.services.georoutes.model.RouteSummary
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleIncident
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleIncidentType
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleLegDetails
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleNotice
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleNoticeCode
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleNoticeDetail
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleSummary
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleTravelStep
import aws.sdk.kotlin.services.georoutes.model.RouteVehicleTravelStepType
import aws.sdk.kotlin.services.location.model.Circle
import aws.sdk.kotlin.services.location.model.GeofenceGeometry
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import java.util.Date

object Responses {
    val RESPONSE_SEARCH_TEXT_RIO_TINTO =
        SearchSuggestionResponse(
            text = "rio tinto",
            maxResults = 5,
            data =
            arrayListOf(
                SearchSuggestionData(
                    placeId = "AQAAAIAACg_vXCRfx8HI5rtgk0Mmnd6PPxpnkNiPXtNOQK-wtPFoIHEB39lZ9bAxMIy5uXFWjHYaOiaMYcI38eByA0ztmt4BmQMA3CvJArxga_45fdoDEIJWlnAfeXv9ExWR1Z32Bwo6K4xtfdSN3EgYLLwjiUEnbyUjTOJdJuy_iwZ8O1p_uCOJDEvvzA5WYaEkFFmGXzTcMxZuYRyKpxlC6EPvWA",
                    searchText = "rio tinto",
                    text = "Rio Tinto, Gondomar, Porto, PRT",
                    distance = 8996050.0,
                    isDestination = false,
                    isPlaceIndexForPosition = false
                ),
                SearchSuggestionData(
                    placeId = "AQAAAIAA3iDmWtWuLicpbqS5M2Mx9Tu7cuOpnFlUvEAyG5bCiAUGHir-hC7W-WjEtmp2wanCf9N1tb0ucKUfTzGyi6OvHIeZHoRI9HkbSBdn-9ZciqUZOTL-c0RIRY4dAYh_V_HtdILotOYkVMLyS7oji7kb263UkGchZeBEs5Zd_Xa3xGJiqnNTsnCDEiIOlOEd3FcBgk8kyXbmj6nRYZylfZ_p7g",
                    searchText = "rio tinto",
                    text = "Rio Tinto, Paraiba, BRA",
                    distance = 1.0205287E7,
                    isDestination = false,
                    isPlaceIndexForPosition = false
                ),
                SearchSuggestionData(
                    placeId = "AQAAAIAAou9Y0RuRsQLJHjkLYIPmxsL5eomLtjMTV77WmN9Kfqj4OaOGLrEeRX7_Vx5JJTXcRuvUQ1Mhm1xrfiXquEPkklYqRQYju-bjuKkUhINJ__xlmV5kguwifl0i3sc4LhUuyzzh_zNZbIROonF7eSeh98fIdklhNXnfH42u8UdkR2NYStBxd0nBDpNf7Sn6U8vp7SJ-a90A8eChHlyaB0PqWA",
                    searchText = "rio tinto",
                    text = "Rio Tinto, Esposende, Braga, PRT",
                    distance = 8961604.0,
                    isDestination = false,
                    isPlaceIndexForPosition = false
                ),
                SearchSuggestionData(
                    placeId = "AQAAAIAATDjvgdm8fMuOOTL4HjSgv90Oqll6po6NbNi8hRyf6Yq7BlxjBs_d0kC17taZ7uB9oy9ZU1zZlq-wFSDSxbW-N4x2vetQsKWyXKMmpK4P0psuyXse1DF3yQ-fG89rKTbv-BKVmkW1hbzS1-3YMKSHil0_4k2R-IrRQ3rUQp8SyFrkS46NGAk4Vb0AeQw3WRGcPxpC4BlQg_6Uap2xNlrDoA",
                    searchText = "rio tinto",
                    text = "Rio Tinto, Amares, Braga, PRT",
                    distance = 8971593.0,
                    isDestination = false,
                    isPlaceIndexForPosition = false
                ),
                SearchSuggestionData(
                    placeId = "AQAAAIAAri9JPWCRlhRHXk7ZzGj8jZj3DT1OKUkNYCIl6SrSdZLpIeufCS1hhhvvnyQS9xc50ZGUvS3-Z-3GZLKzx6YAbrla6nGMwziPkligVgYAtGrJ4n9W2rTrRw3pLzdrh-Bns04nwIVR3CFvVIZu4m6kHIBENAIV_ApVFA2U9smvriVDR2lygNkdYXlUZ0gvwgMyXcnEJXmyakTuRmSvuj71Og",
                    searchText = "rio tinto",
                    text = "Rio Tinto, Vagos, Aveiro, PRT",
                    distance = 9041692.0,
                    isDestination = false,
                    isPlaceIndexForPosition = false
                )
            ),
            error = null
        )

    val RESPONSE_PLACE_INDEX_RIO_TINTO =
        SearchSuggestionResponse(
            text = "Rio Tinto",
            maxResults = 15,
            data =
            arrayListOf(
                SearchSuggestionData(
                    placeId = "test",
                    searchText = "Rio Tinto",
                    text = "Rio Tinto, Gondomar, Porto, PRT",
                    distance = 8996050.0,
                    isDestination = false,
                    isPlaceIndexForPosition = false,
                    position = listOf(72.83468000000005, 18.921880000000044),
                    amazonLocationAddress =
                    Address {
                        country = Country { name = TEST_DATA_2 }
                        label = TEST_DATA_3
                        region = Region { name = TEST_DATA_4 }
                        subRegion = SubRegion { name = TEST_DATA }
                    }
                ),
                SearchSuggestionData(
                    placeId = "test",
                    searchText = "Rio Tinto",
                    text = "Rio Tinto, Paraíba, BRA",
                    distance = 1.0205287E7,
                    isDestination = false,
                    isPlaceIndexForPosition = false,
                    position = listOf(72.83468000000005, 18.921880000000044),
                    amazonLocationAddress =
                    Address {
                        country = Country { name = TEST_DATA_2 }
                        label = TEST_DATA_3
                        region = Region { name = TEST_DATA_4 }
                        subRegion = SubRegion { name = TEST_DATA }
                    }
                )
            ),
            error = null
        )

    var RESPONSE_PLACE_INDEX_RIO_TINTO_ERROR =
        SearchSuggestionResponse(
            error = API_ERROR
        )

    val RESPONSE_CALCULATE_DISTANCE_CAR =
        CalculateRoutesResponse {
            routes =
                listOf(
                    Route {
                        legs =
                            listOf(
                                RouteLeg {
                                    travelMode = RouteLegTravelMode.Car
                                    type = RouteLegType.Vehicle
                                    geometry =
                                        RouteLegGeometry {
                                            lineString =
                                                listOf(
                                                    listOf(72.83371801248408, 18.922163986834548),
                                                    listOf(72.83368999799727, 18.922080011205793),
                                                    listOf(72.83332001005944, 18.92124998876538),
                                                    listOf(72.83318000545705, 18.92106999738339),
                                                    listOf(72.83317999485907, 18.92106998837811),
                                                    listOf(72.83324999683737, 18.921269989185305),
                                                    listOf(72.83338298024672, 18.921575535610945)
                                                )
                                        }
                                    vehicleLegDetails =
                                        RouteVehicleLegDetails {
                                            travelSteps =
                                                listOf(
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 32
                                                        geometryOffset = 1
                                                        type = RouteVehicleTravelStepType.Depart
                                                    },
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 16
                                                        geometryOffset = 4
                                                        type = RouteVehicleTravelStepType.Depart
                                                    }
                                                )
                                            incidents =
                                                listOf(
                                                    RouteVehicleIncident {
                                                        description = "test"
                                                        type = RouteVehicleIncidentType.Other
                                                    }
                                                )
                                            passThroughWaypoints = listOf(
                                                RoutePassThroughWaypoint {
                                                    geometryOffset = 2
                                                    place = RoutePassThroughPlace {
                                                        position = listOf(
                                                            72.83338298024672,
                                                            18.921575535610945
                                                        )
                                                    }
                                                }
                                            )
                                            summary = RouteVehicleSummary {
                                            }
                                            spans = listOf()
                                            tolls = listOf()
                                            tollSystems = listOf()
                                            zones = listOf()
                                            truckRoadTypes = listOf()
                                            notices =
                                                listOf(
                                                    RouteVehicleNotice {
                                                        code = RouteVehicleNoticeCode.Other
                                                        details = listOf(
                                                            RouteVehicleNoticeDetail {
                                                                title = "test"
                                                            }
                                                        )
                                                    }
                                                )
                                        }
                                }
                            )
                        majorRoadLabels = listOf(
                            RouteMajorRoadLabel {
                                roadName = LocalizedString {
                                    value = "test"
                                    language = "en"
                                }
                            }
                        )
                        summary =
                            RouteSummary {
                                distance = 20
                                duration = 48
                            }
                    }
                )
            notices = listOf()
            pricingBucket = "test"
            legGeometryFormat = GeometryFormat.Simple
        }

    val RESPONSE_CALCULATE_DISTANCE_SCOOTER =
        CalculateRoutesResponse {
            routes =
                listOf(
                    Route {
                        legs =
                            listOf(
                                RouteLeg {
                                    travelMode = RouteLegTravelMode.Scooter
                                    type = RouteLegType.Vehicle
                                    geometry =
                                        RouteLegGeometry {
                                            lineString =
                                                listOf(
                                                    listOf(72.83371801248408, 18.922163986834548),
                                                    listOf(72.83368999799727, 18.922080011205793),
                                                    listOf(72.83332001005944, 18.92124998876538),
                                                    listOf(72.83318000545705, 18.92106999738339),
                                                    listOf(72.83317999485907, 18.92106998837811),
                                                    listOf(72.83324999683737, 18.921269989185305),
                                                    listOf(72.83338298024672, 18.921575535610945)
                                                )
                                        }
                                    vehicleLegDetails =
                                        RouteVehicleLegDetails {
                                            travelSteps =
                                                listOf(
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 32
                                                        geometryOffset = 1
                                                        type = RouteVehicleTravelStepType.Depart
                                                    },
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 16
                                                        geometryOffset = 4
                                                        type = RouteVehicleTravelStepType.Depart
                                                    }
                                                )
                                            incidents =
                                                listOf(
                                                    RouteVehicleIncident {
                                                        description = "test"
                                                        type = RouteVehicleIncidentType.Other
                                                    }
                                                )
                                            passThroughWaypoints = listOf(
                                                RoutePassThroughWaypoint {
                                                    geometryOffset = 2
                                                    place = RoutePassThroughPlace {
                                                        position = listOf(
                                                            72.83338298024672,
                                                            18.921575535610945
                                                        )
                                                    }
                                                }
                                            )
                                            summary = RouteVehicleSummary {
                                            }
                                            spans = listOf()
                                            tolls = listOf()
                                            tollSystems = listOf()
                                            zones = listOf()
                                            truckRoadTypes = listOf()
                                            notices =
                                                listOf(
                                                    RouteVehicleNotice {
                                                        code = RouteVehicleNoticeCode.Other
                                                        details = listOf(
                                                            RouteVehicleNoticeDetail {
                                                                title = "test"
                                                            }
                                                        )
                                                    }
                                                )
                                        }
                                }
                            )
                        majorRoadLabels = listOf(
                            RouteMajorRoadLabel {
                                roadName = LocalizedString {
                                    value = "test"
                                    language = "en"
                                }
                            }
                        )
                        summary =
                            RouteSummary {
                                distance = 20
                                duration = 48
                            }
                    }
                )
            notices = listOf()
            pricingBucket = "test"
            legGeometryFormat = GeometryFormat.Simple
        }

    val RESPONSE_CALCULATE_DISTANCE_FERRIES =
        CalculateRoutesResponse {
            routes =
                listOf(
                    Route {
                        legs =
                            listOf(
                                RouteLeg {
                                    travelMode = RouteLegTravelMode.Ferry
                                    type = RouteLegType.Ferry
                                    geometry =
                                        RouteLegGeometry {
                                            lineString =
                                                listOf(
                                                    listOf(72.83371801248408, 18.922163986834548),
                                                    listOf(72.83368999799727, 18.922080011205793),
                                                    listOf(72.83332001005944, 18.92124998876538),
                                                    listOf(72.83318000545705, 18.92106999738339),
                                                    listOf(72.83317999485907, 18.92106998837811),
                                                    listOf(72.83324999683737, 18.921269989185305),
                                                    listOf(72.83338298024672, 18.921575535610945)
                                                )
                                        }
                                    ferryLegDetails =
                                        RouteFerryLegDetails {
                                            afterTravelSteps = listOf()
                                            beforeTravelSteps = listOf()
                                            travelSteps =
                                                listOf(
                                                    RouteFerryTravelStep {
                                                        distance = 10
                                                        duration = 32
                                                        geometryOffset = 1
                                                        type = RouteFerryTravelStepType.Depart
                                                    },
                                                    RouteFerryTravelStep {
                                                        distance = 10
                                                        duration = 16
                                                        geometryOffset = 4
                                                        type = RouteFerryTravelStepType.Depart
                                                    }
                                                )
                                            passThroughWaypoints = listOf(
                                                RoutePassThroughWaypoint {
                                                    geometryOffset = 2
                                                    place = RoutePassThroughPlace {
                                                        position = listOf(
                                                            72.83338298024672,
                                                            18.921575535610945
                                                        )
                                                    }
                                                }
                                            )
                                            summary = RouteFerrySummary {
                                            }
                                            spans = listOf()
                                            notices =
                                                listOf(
                                                    RouteFerryNotice {
                                                        code = RouteFerryNoticeCode.Other
                                                    }
                                                )
                                        }
                                }
                            )
                        majorRoadLabels = listOf(
                            RouteMajorRoadLabel {
                                roadName = LocalizedString {
                                    value = "test"
                                    language = "en"
                                }
                            }
                        )
                        summary =
                            RouteSummary {
                                distance = 20
                                duration = 48
                            }
                    }
                )
            notices = listOf()
            pricingBucket = "test"
            legGeometryFormat = GeometryFormat.Simple
        }

    val RESPONSE_CALCULATE_DISTANCE_WALKING =
        CalculateRoutesResponse {
            routes =
                listOf(
                    Route {
                        legs =
                            listOf(
                                RouteLeg {
                                    travelMode = RouteLegTravelMode.Pedestrian
                                    type = RouteLegType.Pedestrian
                                    geometry =
                                        RouteLegGeometry {
                                            lineString =
                                                listOf(
                                                    listOf(72.83371801248408, 18.922163986834548),
                                                    listOf(72.83368999799727, 18.922080011205793),
                                                    listOf(72.83332001005944, 18.92124998876538),
                                                    listOf(72.83318000545705, 18.92106999738339),
                                                    listOf(72.83317999485907, 18.92106998837811),
                                                    listOf(72.83324999683737, 18.921269989185305),
                                                    listOf(72.83338298024672, 18.921575535610945)
                                                )
                                        }
                                    pedestrianLegDetails =
                                        RoutePedestrianLegDetails {
                                            travelSteps =
                                                listOf(
                                                    RoutePedestrianTravelStep {
                                                        distance = 10
                                                        duration = 32
                                                        geometryOffset = 1
                                                        type = RoutePedestrianTravelStepType.Depart
                                                    },
                                                    RoutePedestrianTravelStep {
                                                        distance = 10
                                                        duration = 16
                                                        geometryOffset = 4
                                                        type = RoutePedestrianTravelStepType.Depart
                                                    }
                                                )
                                            passThroughWaypoints = listOf(
                                                RoutePassThroughWaypoint {
                                                    geometryOffset = 2
                                                    place = RoutePassThroughPlace {
                                                        position = listOf(
                                                            72.83338298024672,
                                                            18.921575535610945
                                                        )
                                                    }
                                                }
                                            )
                                            summary = RoutePedestrianSummary {
                                            }
                                            spans = listOf()
                                            notices =
                                                listOf(
                                                    RoutePedestrianNotice {
                                                        code = RoutePedestrianNoticeCode.Other
                                                    }
                                                )
                                        }
                                }
                            )
                        majorRoadLabels = listOf(
                            RouteMajorRoadLabel {
                                roadName = LocalizedString {
                                    value = "test"
                                    language = "en"
                                }
                            }
                        )
                        summary =
                            RouteSummary {
                                distance = 20
                                duration = 48
                            }
                    }
                )
            notices = listOf()
            pricingBucket = "test"
            legGeometryFormat = GeometryFormat.Simple
        }

    val RESPONSE_CALCULATE_DISTANCE_TRUCK =
        CalculateRoutesResponse {
            routes =
                listOf(
                    Route {
                        legs =
                            listOf(
                                RouteLeg {
                                    travelMode = RouteLegTravelMode.Truck
                                    type = RouteLegType.Vehicle
                                    geometry =
                                        RouteLegGeometry {
                                            lineString =
                                                listOf(
                                                    listOf(72.83371801248408, 18.922163986834548),
                                                    listOf(72.83368999799727, 18.922080011205793),
                                                    listOf(72.83332001005944, 18.92124998876538),
                                                    listOf(72.83318000545705, 18.92106999738339),
                                                    listOf(72.83317999485907, 18.92106998837811),
                                                    listOf(72.83324999683737, 18.921269989185305),
                                                    listOf(72.83338298024672, 18.921575535610945)
                                                )
                                        }
                                    vehicleLegDetails =
                                        RouteVehicleLegDetails {
                                            travelSteps =
                                                listOf(
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 32
                                                        geometryOffset = 1
                                                        type = RouteVehicleTravelStepType.Depart
                                                    },
                                                    RouteVehicleTravelStep {
                                                        distance = 10
                                                        duration = 16
                                                        geometryOffset = 4
                                                        type = RouteVehicleTravelStepType.Depart
                                                    }
                                                )
                                            incidents =
                                                listOf(
                                                    RouteVehicleIncident {
                                                        description = "test"
                                                        type = RouteVehicleIncidentType.Other
                                                    }
                                                )
                                            passThroughWaypoints = listOf(
                                                RoutePassThroughWaypoint {
                                                    geometryOffset = 2
                                                    place = RoutePassThroughPlace {
                                                        position = listOf(
                                                            72.83338298024672,
                                                            18.921575535610945
                                                        )
                                                    }
                                                }
                                            )
                                            summary = RouteVehicleSummary {
                                            }
                                            spans = listOf()
                                            tolls = listOf()
                                            tollSystems = listOf()
                                            zones = listOf()
                                            truckRoadTypes = listOf()
                                            notices =
                                                listOf(
                                                    RouteVehicleNotice {
                                                        code = RouteVehicleNoticeCode.Other
                                                        details = listOf(
                                                            RouteVehicleNoticeDetail {
                                                                title = "test"
                                                            }
                                                        )
                                                    }
                                                )
                                        }
                                }
                            )
                        majorRoadLabels = listOf(
                            RouteMajorRoadLabel {
                                roadName = LocalizedString {
                                    value = "test"
                                    language = "en"
                                }
                            }
                        )
                        summary =
                            RouteSummary {
                                distance = 20
                                duration = 48
                            }
                    }
                )
            notices = listOf()
            pricingBucket = "test"
            legGeometryFormat = GeometryFormat.Simple
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
            country = "IND"
        )

    val GET_PLACE_RESPONSE = GetPlaceResponse {
        placeId = "test"
        placeType = PlaceType.Block
        pricingBucket = "test"
        title = "place"
        contacts = Contacts {
            phones = listOf(
                ContactDetails {
                    label = "test"
                    value = "6666444422"
                }
            )
        }
    }

    val SEARCH_RESPONSE_TAJ =
        SearchResponse(
            reverseGeocodeResponse =
            ReverseGeocodeResponse {
                pricingBucket = "test"
                resultItems =
                    listOf(
                        ReverseGeocodeResultItem {
                            placeId = "test"
                            title = "testTitle"
                            placeType = PlaceType.Block
                            distance = 0L
                            position = listOf(TEST_DATA_LNG, TEST_DATA_LAT)
                            address =
                                Address {
                                    country = Country { name = TEST_DATA_2 }
                                    label = TEST_DATA_3
                                    region = Region { name = TEST_DATA_4 }
                                    subRegion = SubRegion { name = TEST_DATA }
                                }
                        }
                    )
            },
            latitude = TEST_DATA_LAT,
            longitude = TEST_DATA_LNG
        )

    val RESPONSE_ADDRESS_LINE_FROM_LAT_LNG =
        SearchResponse(
            reverseGeocodeResponse =
            ReverseGeocodeResponse {
                pricingBucket = "test"
                resultItems =
                    listOf(
                        ReverseGeocodeResultItem {
                            placeId = "test"
                            title = "testTitle"
                            placeType = PlaceType.Block
                            distance = 0L
                            position = listOf(72.83468000000005, 18.921880000000044)
                            address =
                                Address {
                                    country = Country { name = "IND" }
                                    label = "Gateway of India, Mumbai, Mahārāshtra, IND"
                                    region = Region { name = "Mahārāshtra" }
                                    subRegion = SubRegion { name = "Mumbai" }
                                }
                        }
                    )
            },
            latitude = TEST_DATA_LAT,
            longitude = TEST_DATA_LNG
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
                    geometry =
                        GeofenceGeometry {
                            circle =
                                Circle {
                                    center = listOf(72.530537125, 23.0147063)
                                    radius = 778.0
                                }
                        }
                }
            )
        )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST =
        GeofenceData(
            message = MOCK_ERROR,
            geofenceList = arrayListOf()
        )

    val RESPONSE_ERROR_TRACKER_GEOFENCE_LIST_NO_ERROR =
        GeofenceData(
            message = null,
            geofenceList = arrayListOf()
        )
}
