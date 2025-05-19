package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofenceHelper.turf.TurfMeta
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsPointTest {

    @Test
    fun turfMetaCordPolygonLatLngTest() {
        val pointList = TurfMeta.coordAll(
            Polygon.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        Point.fromLngLat(
                            TEST_DATA_LNG,
                            TEST_DATA_LAT
                        )
                    )
                )
            ),
            false
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordMultiLineStringTest() {
        val pointList = TurfMeta.coordAll(
            MultiLineString.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        Point.fromLngLat(
                            TEST_DATA_LNG,
                            TEST_DATA_LAT
                        )
                    )
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
