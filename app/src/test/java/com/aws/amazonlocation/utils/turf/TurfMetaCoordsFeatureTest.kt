package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.jsonTurf
import com.aws.amazonlocation.mock.jsonTurf1
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsFeatureTest {

    @Test
    fun turfMetaCordFeatureTest() {
        val feature: Feature = Feature.fromJson(jsonTurf)
        val pointListWithExcludeWrap = TurfMeta.coordAll(feature, true)
        Assert.assertTrue(pointListWithExcludeWrap.isNotEmpty())
        val pointList = TurfMeta.coordAll(feature, false)
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordFeatureCollectionTest() {
        val feature: FeatureCollection = FeatureCollection.fromJson(jsonTurf1)
        val pointList = TurfMeta.coordAll(feature, false)
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
