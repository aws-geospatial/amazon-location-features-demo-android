package com.aws.amazonlocation.utils.geofence_helper.turf;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * This class is made up of methods that take in an object, convert it, and then return the object
 * in the desired units or object.
 *
 * @see <a href="http://turfjs.org/docs/">Turfjs documentation</a>
 * @since 1.2.0
 */
public final class TurfConversion {

    private static final Map<String, Double> FACTORS;

    static {
        FACTORS = new HashMap<>();
        FACTORS.put(TurfConstants.UNIT_MILES, 3960d);
        FACTORS.put(TurfConstants.UNIT_NAUTICAL_MILES, 3441.145d);
        FACTORS.put(TurfConstants.UNIT_DEGREES, 57.2957795d);
        FACTORS.put(TurfConstants.UNIT_RADIANS, 1d);
        FACTORS.put(TurfConstants.UNIT_INCHES, 250905600d);
        FACTORS.put(TurfConstants.UNIT_YARDS, 6969600d);
        FACTORS.put(TurfConstants.UNIT_METERS, 6373000d);
        FACTORS.put(TurfConstants.UNIT_CENTIMETERS, 6.373e+8d);
        FACTORS.put(TurfConstants.UNIT_KILOMETERS, 6373d);
        FACTORS.put(TurfConstants.UNIT_FEET, 20908792.65d);
        FACTORS.put(TurfConstants.UNIT_CENTIMETRES, 6.373e+8d);
        FACTORS.put(TurfConstants.UNIT_METRES, 6373000d);
        FACTORS.put(TurfConstants.UNIT_KILOMETRES, 6373d);
    }

    private TurfConversion() {
        // Private constructor preventing initialization of this class
    }

    /**
     * Converts an angle in degrees to radians.
     *
     * @param degrees angle between 0 and 360 degrees
     * @return angle in radians
     * @since 3.1.0
     */
    public static double degreesToRadians(double degrees) {
        double radians = degrees % 360;
        return radians * Math.PI / 180;
    }

    /**
     * Converts an angle in radians to degrees.
     *
     * @param radians angle in radians
     * @return degrees between 0 and 360 degrees
     * @since 3.0.0
     */
    public static double radiansToDegrees(double radians) {
        double degrees = radians % (2 * Math.PI);
        return degrees * 180 / Math.PI;
    }

    /**
     * Convert a distance measurement (assuming a spherical Earth) from radians to a more friendly
     * unit.
     *
     * @param radians a double using unit radian
     * @param units pass in one of the units defined in {@link TurfConstants.TurfUnitCriteria}
     * @return converted radian to distance value
     * @since 1.2.0
     */
    public static double radiansToLength(double radians, @NonNull @TurfConstants.TurfUnitCriteria
    String units) {
        return radians * FACTORS.get(units);
    }

    /**
     * Convert a distance measurement (assuming a spherical Earth) from a real-world unit into
     * radians.
     *
     * @param distance double representing a distance value
     * @param units pass in one of the units defined in {@link TurfConstants.TurfUnitCriteria}
     * @return converted distance to radians value
     * @since 1.2.0
     */
    public static double lengthToRadians(double distance, @NonNull @TurfConstants.TurfUnitCriteria
    String units) {
        return distance / FACTORS.get(units);
    }
}