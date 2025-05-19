package com.aws.amazonlocation.utils.geofenceHelper.turf;

import com.google.gson.JsonObject;

import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import java.util.List;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.aws.amazonlocation.utils.geofenceHelper.turf.TurfConversion.degreesToRadians;
import static com.aws.amazonlocation.utils.geofenceHelper.turf.TurfConversion.radiansToDegrees;

/**
 * Class contains an assortment of methods used to calculate measurements such as bearing,
 * destination, midpoint, etc.
 *
 * @see <a href="http://turfjs.org/docs/">Turf documentation</a>
 * @since 1.2.0
 */
public final class TurfMeasurement {

    private TurfMeasurement() {
        throw new AssertionError("No Instances.");
    }

    /**
     * Takes a Point and calculates the location of a destination point given a distance in
     * degrees, radians, miles, or kilometers; and bearing in degrees. This uses the Haversine
     * formula to account for global curvature.
     *
     * @param point starting point used for calculating the destination
     * @param distance distance from the starting point
     * @param bearing ranging from -180 to 180 in decimal degrees
     * @param units one of the units found inside {@link TurfConstants.TurfUnitCriteria}
     * @return destination {@link Point} result where you specified
     * @see <a href="http://turfjs.org/docs/#destination">Turf Destination documetation</a>
     * @since 1.2.0
     */
    @NonNull
    public static Point destination(@NonNull Point point, @FloatRange(from = 0) double distance,
            @FloatRange(from = -180, to = 180) double bearing,
            @NonNull @TurfConstants.TurfUnitCriteria String units) {

        double longitude1 = degreesToRadians(point.longitude());
        double latitude1 = degreesToRadians(point.latitude());
        double bearingRad = degreesToRadians(bearing);

        double radians = TurfConversion.lengthToRadians(distance, units);

        double latitude2 = Math.asin(Math.sin(latitude1) * Math.cos(radians)
                + Math.cos(latitude1) * Math.sin(radians) * Math.cos(bearingRad));
        double longitude2 = longitude1 + Math.atan2(Math.sin(bearingRad)
                        * Math.sin(radians) * Math.cos(latitude1),
                Math.cos(radians) - Math.sin(latitude1) * Math.sin(latitude2));

        return Point.fromLngLat(
                radiansToDegrees(longitude2), radiansToDegrees(latitude2));
    }

    /**
     * Calculates the distance between two points in kilometers. This uses the Haversine formula to
     * account for global curvature.
     *
     * @param point1 first point used for calculating the bearing
     * @param point2 second point used for calculating the bearing
     * @return distance between the two points in kilometers
     * @see <a href="http://turfjs.org/docs/#distance">Turf distance documentation</a>
     * @since 1.2.0
     */
    public static double distance(@NonNull Point point1, @NonNull Point point2) {
        return distance(point1, point2, TurfConstants.UNIT_DEFAULT);
    }

    /**
     * Calculates the distance between two points in degress, radians, miles, or kilometers. This
     * uses the Haversine formula to account for global curvature.
     *
     * @param point1 first point used for calculating the bearing
     * @param point2 second point used for calculating the bearing
     * @param units one of the units found inside {@link TurfConstants.TurfUnitCriteria}
     * @return distance between the two points in kilometers
     * @see <a href="http://turfjs.org/docs/#distance">Turf distance documentation</a>
     * @since 1.2.0
     */
    public static double distance(@NonNull Point point1, @NonNull Point point2,
            @NonNull @TurfConstants.TurfUnitCriteria String units) {
        double difLat = degreesToRadians((point2.latitude() - point1.latitude()));
        double difLon = degreesToRadians((point2.longitude() - point1.longitude()));
        double lat1 = degreesToRadians(point1.latitude());
        double lat2 = degreesToRadians(point2.latitude());

        double value = Math.pow(Math.sin(difLat / 2), 2)
                + Math.pow(Math.sin(difLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);

        return TurfConversion.radiansToLength(
                2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value)), units);
    }

    /**
     * Takes a set of features, calculates the bbox of all input features, and returns a bounding box.
     *
     * @param featureCollection a {@link FeatureCollection} object
     * @return a double array defining the bounding box in this order {@code [minX, minY, maxX, maxY]}
     * @since 4.8.0
     */
    public static double[] bbox(FeatureCollection featureCollection) {
        return bboxCalculator(TurfMeta.coordAll(featureCollection, false));
    }

    private static double[] bboxCalculator(List<Point> resultCoords) {
        double[] bbox = new double[4];

        bbox[0] = Double.POSITIVE_INFINITY;
        bbox[1] = Double.POSITIVE_INFINITY;
        bbox[2] = Double.NEGATIVE_INFINITY;
        bbox[3] = Double.NEGATIVE_INFINITY;

        for (Point point : resultCoords) {
            if (bbox[0] > point.longitude()) {
                bbox[0] = point.longitude();
            }
            if (bbox[1] > point.latitude()) {
                bbox[1] = point.latitude();
            }
            if (bbox[2] < point.longitude()) {
                bbox[2] = point.longitude();
            }
            if (bbox[3] < point.latitude()) {
                bbox[3] = point.latitude();
            }
        }
        return bbox;
    }
    /**
     * Takes a {@link Feature} and returns the absolute center of the {@link Feature}.
     *
     * @param feature the single {@link Feature} to find the center of.
     * @param properties a optional {@link JsonObject} containing the properties that should be
     * placed in the returned {@link Feature}.
     * @param id an optional common identifier that should be placed in the returned {@link Feature}.
     * @return a {@link Feature} with a {@link Point} geometry type.
     * @since 5.3.0
     */
    public static Feature center(Feature feature,
            @Nullable JsonObject properties,
            @Nullable String id) {
        return center(FeatureCollection.fromFeature(feature), properties, id);
    }

    /**
     * Takes a {@link Feature} and returns the absolute center of the {@link Feature}.
     *
     * @param feature the single {@link Feature} to find the center of.
     * @return a {@link Feature} with a {@link Point} geometry type.
     * @since 5.3.0
     */
    public static Feature center(Feature feature) {
        return center(FeatureCollection.fromFeature(feature), null, null);
    }

    /**
     * Takes {@link FeatureCollection} and returns the absolute center
     * of the {@link Feature}s in the {@link FeatureCollection}.
     *
     * @param featureCollection the single {@link FeatureCollection} to find the center of.
     * @param properties a optional {@link JsonObject} containing the properties that should be
     * placed in the returned {@link Feature}.
     * @param id an optional common identifier that should be placed in the returned {@link Feature}.
     * @return a {@link Feature} with a {@link Point} geometry type.
     * @since 5.3.0
     */
    public static Feature center(FeatureCollection featureCollection,
            @Nullable JsonObject properties,
            @Nullable String id) {
        double[] ext = bbox(featureCollection);
        double finalCenterLongitude = (ext[0] + ext[2]) / 2;
        double finalCenterLatitude = (ext[1] + ext[3]) / 2;
        return Feature.fromGeometry(Point.fromLngLat(finalCenterLongitude, finalCenterLatitude),
                properties, id);
    }

    /**
     * Takes {@link FeatureCollection} and returns the absolute center
     * of the {@link Feature}s in the {@link FeatureCollection}.
     *
     * @param featureCollection the single {@link FeatureCollection} to find the center of.
     * @return a {@link Feature} with a {@link Point} geometry type.
     * @since 5.3.0
     */
    public static Feature center(FeatureCollection featureCollection) {
        return center(featureCollection, null, null);
    }
}