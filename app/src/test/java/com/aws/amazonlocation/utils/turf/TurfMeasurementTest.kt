package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofenceHelper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofenceHelper.turf.TurfMeasurement
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.Point
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMeasurementTest {

    @Test
    fun turfMeasurementDistanceTest() {
        val distance = TurfMeasurement.distance(
            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT)
        )
        Assert.assertTrue(!distance.isNaN())
        val destination = TurfMeasurement.destination(
            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
            10.654654,
            10.5,
            TurfConstants.UNIT_MILES
        )
        Assert.assertTrue(!destination.latitude().isNaN())
    }
}
