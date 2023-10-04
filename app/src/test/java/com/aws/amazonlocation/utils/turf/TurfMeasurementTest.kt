package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.mock.jsonTurf
import com.aws.amazonlocation.mock.jsonTurf1
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeasurement
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMeasurementTest {

    @Test
    fun turfMeasurementDistanceTest() {
        val distance = TurfMeasurement.distance(
            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
        )
        Assert.assertTrue(!distance.isNaN())
        val destination = TurfMeasurement.destination(
            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
            10.654654,
            10.5,
            TurfConstants.UNIT_MILES
        )
        Assert.assertTrue(!destination.latitude().isNaN())
    }
}
