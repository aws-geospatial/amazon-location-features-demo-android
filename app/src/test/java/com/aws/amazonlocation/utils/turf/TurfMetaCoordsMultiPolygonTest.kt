package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofenceHelper.turf.TurfMeta
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.MultiPolygon
import org.maplibre.geojson.Point
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
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT)
                        )
                    )
                )
            ),
            true
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
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT),
                            Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT)
                        )
                    )
                )
            ),
            false
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
