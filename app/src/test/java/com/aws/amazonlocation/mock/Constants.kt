package com.aws.amazonlocation.mock

import java.util.Locale
import org.maplibre.android.geometry.LatLng

const val DELAY_1000 = 1000L

val DEFAULT_LOCATION = LatLng(49.281174, -123.116823)
const val DEVICE_ID = "662f86eddc909886"

const val SEARCH_TEXT_RIO_TINTO = "rio tinto"
val GATE_WAY_OF_INDIA_LAT_LNG = LatLng(18.921880000000044, 72.83468000000005) // Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_FROM = LatLng(18.92216, 72.83373) // Near Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
const val TEST_DATA_LAT = 72.83312000000006
const val TEST_DATA_LNG = 18.92169000000007
const val TEST_DATA_LAT_1 = -122.084
const val TEST_DATA_LNG_1 = 37.421998333333335
val DISTANCE_COORDINATE_TO = LatLng(TEST_DATA_LNG, TEST_DATA_LAT) // The Taj Palace Hotel, Mumbai, Mumbai, Mahārāshtra, IND
const val AVOID_FERRIES = false
const val AVOID_TOLLS = true

const val VALID_LAT = 18.921880000000044
const val VALID_LNG = 72.83468000000005
const val INVALID_LAT = 91.0
const val INVALID_LNG = 181.0
const val TURF_TOLERANCE = 10.0

const val LAT_LNG_VALID_STRING = "$VALID_LAT, $VALID_LNG"
const val INVALID_LNG_STRING = "$VALID_LAT, $INVALID_LNG"
const val INVALID_LAT_STRING = "$INVALID_LAT, $VALID_LNG"

const val STANDARD = "Standard"

const val SIGN_IN_SUCCESS = "Sign in Successfully"

const val TRACKING_HISTORY_START_DATE = "Thu Apr 14 00:00:00 GMT+05:30 2022"
const val TRACKING_HISTORY_END_DATE = "Fri Apr 14 23:59:59 GMT+05:30 2023"
const val TRACKING_HISTORY_TODAY_START_DATE = "Fri Apr 14 00:00:00 GMT+05:30 2023"
const val TRACKING_HISTORY_TODAY_END_DATE = "Fri Apr 14 23:59:59 GMT+05:30 2023"

const val START_DATE_NULL = "Start date is null"
const val END_DATE_NULL = "End date is null"

const val MOCK_ERROR = "Mock error"
const val API_ERROR = "Please try again later"
const val NO_INTERNET_ERROR = "Check your internet connection and try again"

const val TEST_FAILED_DUE_TO_STATE_NOT_LOADING = "Test failed due to state not loading"
const val TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS = "Test failed due to state not success"
const val TEST_FAILED_DUE_TO_STATE_NOT_ERROR = "Test failed due to state not error"
const val TEST_FAILED_DUE_TO_INCORRECT_DATA = "Test failed due to incorrect data"
const val TEST_FAILED_DUE_TO_DATA_NOT_EMPTY = "Test failed due to data not empty"
const val TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE = "Test failed due to incorrect data size"
const val TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE = "Test failed due to incorrect error message"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR = "Test failed due to incorrect no internet error"
const val TEST_FAILED_DUE_TO_ROBOLECTRIC_TEST_RUNNING = "Test failed due to robolectric test running"

const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_LANGUAGE_LOADED = "Test failed due to incorrect no of language loaded"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED = "Test failed due to incorrect no of providers loaded"
const val TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_STANDARD = "Test failed due to incorrect style name for Standard"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_STANDARD = "Test failed due to incorrect no of styles loaded for Standard"

const val TEST_FAILED_NAVIGATION_DATA = "Navigation data not match"
const val TEST_FAILED_LOGIN_DATA = "Login data not match"
const val TEST_FAILED_SEARCH_SUGGESTION_DATA = "Search suggestion data not match"
const val TEST_FAILED_SEARCH_DATA = "Search data not match"
const val TEST_FAILED_UPDATE_BATCH_DATA = "Update batch Location data not match"
const val TEST_FAILED_ADD_GEOFENCE_DATA = "Add geofence data not match"
const val TEST_FAILED_SIGN_OUT_DATA = "Sign out data not match"
const val TEST_FAILED_CALCULATE_DISTANCE_DATA = "Calculate distance data not match"
const val TEST_FAILED_TRACKING_HISTORY_DATA = "Tracking history data not match"
const val TEST_FAILED_MAP_STYLE_INNER_DATA = "Map style inner data not match"
const val TEST_FAILED_MAP_STYLE_DATA = "Map style data not match"

const val NO_DATA_FOUND = "No data found"
const val TEST_DATA = "Mumbai"
const val TEST_DATA_2 = "IND"
const val TEST_DATA_3 = "The Taj, Mumbai, Mahārāshtra, IND"
const val TEST_DATA_4 = "Mahārāshtra"
const val TEST_DATA_5 = "test"
const val TEST_DATA_7 = "ACTIVE"
const val TEST_DATA_8 = "jj"
const val TEST_DATA_9 = "11"

const val TEST_FAILED_DUE_TO_INVALID_LATITUDE = "Test failed due to invalid latitude"
const val TEST_FAILED_DUE_TO_INVALID_LONGITUDE = "Test failed due to invalid longitude"
const val TEST_FAILED_DUE_TO_VALID_LONGITUDE = "Test failed due to valid longitude"
const val TEST_FAILED_DUE_TO_VALID_LATITUDE = "Test failed due to valid latitude"

val GET_REGION_SUBREGION = "Gondomar"
val GET_REGION_REGION = "Porto"
val GET_REGION_COUNTRY = "PRT"

val GET_REGION_EMPTY = ""
val GET_REGION_SUBREGION_COUNTRY = "Gondomar, PRT"
val GET_REGION_REGION_COUNTRY = "Porto, PRT"

const val PREF_MANAGER_KEY_STRING = "string"
const val PREF_MANAGER_KEY_INT = "int"
const val PREF_MANAGER_KEY_DOUBLE = "double"
const val PREF_MANAGER_KEY_LONG = "long"
const val PREF_MANAGER_KEY_STRING_SET = "string_set"
const val PREF_MANAGER_KEY_BOOL = "bool"

const val PREF_MANAGER_VALUE_STRING = "string"
const val PREF_MANAGER_VALUE_INT = 1
const val PREF_MANAGER_VALUE_DOUBLE = 1.0
const val PREF_MANAGER_VALUE_LONG = 1L
val PREF_MANAGER_VALUE_STRING_SET = setOf("string1", "string2")
const val PREF_MANAGER_VALUE_BOOL = true

const val UNIT_METRICS_M_INPUT_1 = 1000.0
const val UNIT_METRICS_M_INPUT_2 = 1001.0
const val UNIT_METRICS_M_OUTPUT_2 = "1 km"
const val UNIT_METRICS_M_OUTPUT_3 = "1,000 m"
const val UNIT_METRICS_I_INPUT_1 = 5280.0
const val UNIT_METRICS_I_INPUT_2 = 5281.0
const val UNIT_METRICS_I_OUTPUT_2 = "1 mi"
const val UNIT_METRICS_I_OUTPUT_3 = "5,280 ft"

const val UNIT_TIME_SEC_1 = 50L
const val UNIT_TIME_SEC_2 = 120L
const val UNIT_TIME_SEC_3 = 3600L

const val UNIT_TIME_1_OUTPUT = "50 sec"
const val UNIT_TIME_2_OUTPUT = "2 min"
const val UNIT_TIME_3_OUTPUT = "1 hr 0 min"
val LOCALE_IN = Locale("en", "IN")
val LOCALE_US = Locale.US
val METRIC = "Metric"
val IMPERIAL = "Imperial"
val AUTOMATIC = "Automatic"

// Dummy credential for test case
val IDENTITY_POOL_CLIENT_ID_TEST = "66afllav6ri6hf66sikh6t6foh"
val USER_POOL_ID_TEST = "ca-central-1_X6aaHHHb6"
val jsonTurf: String = """
                {
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [102.0, 0.5]
  },
  "properties": {
    "prop0": "value0"
  }
}
                """
val jsonTurf1: String = """
               {
  "type": "FeatureCollection",
  "bbox": [100.0, 0.0, -100.0, 105.0, 1.0, 0.0],
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [102.0, 0.5]
      },
      "properties": {
        "prop0": "value0"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [102.0, 0.0],
          [103.0, 1.0],
          [104.0, 0.0]
        ]
      },
      "properties": {
        "prop0": "value1"
      }
    }
    // You can add more feature objects here as needed
  ]
}
                """
const val option1 = "Option 1"
const val option2 = "Option 2"
const val option3 = "Option 3"
const val option4 = "Option 4"
const val option5 = "Option 5"
const val hour_1 = "1 hour"
const val DESTINATION = "Destination"
const val CITY_NAME = "CityName"
const val REGION_NAME = "RegionName"
const val SUB_REGION_NAME = "SubRegionName"
const val COUNTRY_NAME = "CountryName"
const val TEST_NOTIFICATION = "TestNotification"
const val GEOFENCE_COLLECTION = "GeofenceCollection"
const val ROUTE_ID = "RouteId"
const val ROUTE_NAME = "RouteName"
const val POINT = "Point"
const val STOP_123 = "stop123"
const val STOP_456 = "stop456"
const val BUS_STOP = "Bus Stop"
const val BUS_STATION = "Bus Station"
const val FEATURE = "Feature"