package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
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
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        )
                    )
                )
            ), false
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
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        )
                    )
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
