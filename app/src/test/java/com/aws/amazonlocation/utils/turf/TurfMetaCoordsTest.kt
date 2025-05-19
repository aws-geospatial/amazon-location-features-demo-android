package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofenceHelper.turf.TurfMeta
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiPoint
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsTest {

    @Test
    fun turfMetaCordTest() {
        val pointList = TurfMeta.coordAll(Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT))
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllTest() {
        val pointList = TurfMeta.coordAll(
            MultiPoint.fromLngLats(
                mutableListOf(
                    Point.fromLngLat(
                        TEST_DATA_LNG,
                        TEST_DATA_LAT
                    ),
                    Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT)
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
                        TEST_DATA_LNG,
                        TEST_DATA_LAT
                    ),
                    Point.fromLngLat(TEST_DATA_LNG, TEST_DATA_LAT)
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
                            TEST_DATA_LNG,
                            TEST_DATA_LAT
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LNG,
                            TEST_DATA_LAT
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LNG,
                            TEST_DATA_LAT
                        )
                    )
                )
            ),
            true
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
