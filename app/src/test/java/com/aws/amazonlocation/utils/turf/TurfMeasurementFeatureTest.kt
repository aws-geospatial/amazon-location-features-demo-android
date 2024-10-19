package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_5
import com.aws.amazonlocation.mock.jsonTurf
import com.aws.amazonlocation.mock.jsonTurf1
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeasurement
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMeasurementFeatureTest {

    @Test
    fun turfMeasurementDistanceTest() {
        val feature: Feature = Feature.fromJson(jsonTurf)
        val featureCollection: FeatureCollection = FeatureCollection.fromJson(jsonTurf1)
        val doubles = TurfMeasurement.bbox(featureCollection)
        Assert.assertTrue(!doubles[0].isNaN())
        val center = TurfMeasurement.center(featureCollection)
        Assert.assertTrue(center != null)
        val centerFeature = TurfMeasurement.center(feature)
        Assert.assertTrue(centerFeature != null)
        val centerFeatureProperties = TurfMeasurement.center(feature, null, TEST_DATA_5)
        Assert.assertTrue(centerFeatureProperties != null)
        val centerWithId = TurfMeasurement.center(featureCollection, null, TEST_DATA_5)
        Assert.assertTrue(centerWithId != null)
    }
}