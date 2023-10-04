package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsTest {

    @Test
    fun turfMetaCordTest() {
        val pointList = TurfMeta.coordAll(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG))
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllTest() {
        val pointList = TurfMeta.coordAll(
            MultiPoint.fromLngLats(
                mutableListOf(
                    Point.fromLngLat(
                        TEST_DATA_LAT,
                        TEST_DATA_LNG
                    ), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllLineStringTest() {
        val pointList = TurfMeta.coordAll(
            LineString.fromLngLats(
                mutableListOf(
                    Point.fromLngLat(
                        TEST_DATA_LAT,
                        TEST_DATA_LNG
                    ), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllPolygonTest() {
        val pointList = TurfMeta.coordAll(
            Polygon.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        )
                    )
                )
            ), true
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
