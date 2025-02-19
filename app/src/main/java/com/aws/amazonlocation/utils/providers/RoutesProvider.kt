package com.aws.amazonlocation.utils.providers

import aws.sdk.kotlin.services.georoutes.GeoRoutesClient
import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesRequest
import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import aws.sdk.kotlin.services.georoutes.model.GeometryFormat
import aws.sdk.kotlin.services.georoutes.model.MeasurementSystem
import aws.sdk.kotlin.services.georoutes.model.RouteAvoidanceOptions
import aws.sdk.kotlin.services.georoutes.model.RouteLegAdditionalFeature
import aws.sdk.kotlin.services.georoutes.model.RouteTravelMode
import aws.sdk.kotlin.services.georoutes.model.RouteTravelStepType
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.AvoidanceOption
import com.aws.amazonlocation.ui.main.explore.DepartOption
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units.isMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutesProvider(
    private var mPreferenceManager: PreferenceManager,
) {
    suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        avoidanceOptions: ArrayList<AvoidanceOption>,
        departOption: String,
        travelMode: String?,
        time: String? = null,
        mBaseActivity: BaseActivity?,
        getRoutesClient: GeoRoutesClient?,
    ): CalculateRoutesResponse? =
        try {
            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, "Automatic"))

            val routeTravelMode =
                when (travelMode) {
                    RouteTravelMode.Car.value -> RouteTravelMode.Car
                    RouteTravelMode.Truck.value -> RouteTravelMode.Truck
                    RouteTravelMode.Scooter.value -> RouteTravelMode.Scooter
                    else -> RouteTravelMode.Pedestrian
                }

            val request =
                CalculateRoutesRequest {
                    origin = listOfNotNull(lngDeparture, latDeparture)
                    destination = listOfNotNull(lngDestination, latDestination)
                    avoid =
                        RouteAvoidanceOptions {
                            ferries =
                                AvoidanceOption.FERRIES
                                    .takeIf { it in avoidanceOptions }
                                    ?.let { true }
                            tollRoads =
                                AvoidanceOption.TOLL_ROADS
                                    .takeIf { it in avoidanceOptions }
                                    ?.let { true }
                            dirtRoads =
                                AvoidanceOption.DIRT_ROADS
                                    .takeIf { it in avoidanceOptions }
                                    ?.let { true }
                            tunnels =
                                AvoidanceOption.TUNNELS
                                    .takeIf { it in avoidanceOptions }
                                    ?.let { true }
                            if (routeTravelMode != RouteTravelMode.Scooter && routeTravelMode != RouteTravelMode.Pedestrian) {
                                uTurns =
                                    AvoidanceOption.U_TURNS
                                        .takeIf { it in avoidanceOptions }
                                        ?.let { true }
                            }
                        }
                    legGeometryFormat = GeometryFormat.Simple
                    instructionsMeasurementSystem =
                        if (isMetric) MeasurementSystem.Metric else MeasurementSystem.Imperial
                    when (DepartOption.valueOf(departOption)) {
                        DepartOption.LEAVE_NOW -> departNow = true
                        DepartOption.DEPART_TIME -> {
                            departureTime = time
                        }
                        DepartOption.ARRIVE_TIME -> {
                            arrivalTime = time
                        }
                    }
                    this.travelMode = routeTravelMode
                    travelStepType = RouteTravelStepType.Default
                    legAdditionalFeatures =
                        listOf(
                            RouteLegAdditionalFeature.TravelStepInstructions,
                            RouteLegAdditionalFeature.Summary,
                        )
                }

            withContext(Dispatchers.IO) {
                getRoutesClient?.calculateRoutes(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, "")
            null
        }
}
