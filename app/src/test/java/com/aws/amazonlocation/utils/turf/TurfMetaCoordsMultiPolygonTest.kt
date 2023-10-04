package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsMultiPolygonTest {

    @Test
    fun turfMetaCordMultiPolygonTest() {
        val pointList = TurfMeta.coordAll(
            MultiPolygon.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        mutableListOf(
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                        )
                    )
                )
            ), true
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordMultiPolygonLatLngTest() {
        val pointList = TurfMeta.coordAll(
            MultiPolygon.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        mutableListOf(
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG),
                            Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                        )
                    )
                )
            ), false
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
