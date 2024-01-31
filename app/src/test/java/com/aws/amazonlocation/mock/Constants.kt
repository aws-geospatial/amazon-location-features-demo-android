package com.aws.amazonlocation.mock

import com.amazonaws.services.geo.model.Place
import com.amazonaws.services.geo.model.PlaceGeometry
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.models.Coordinates
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Locale

const val DELAY_1000 = 1000L
const val TIMEOUT_5000 = 5000L

val DEFAULT_LOCATION = LatLng(49.281174, -123.116823)
const val DEVICE_ID = "662f86eddc909886"

const val SEARCH_TEXT_RIO_TINTO = "rio tinto"
const val SEARCH_TEXT_TINTO = "Tinto"
const val SEARCH_TEXT_ERROR = "asdfghjk"
val GATE_WAY_OF_INDIA_LAT_LNG = LatLng(18.921880000000044, 72.83468000000005) // Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_FROM = LatLng(18.92216, 72.83373) // Near Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val FAKE_LAT_LNG = LatLng(49.2811, -12.3116823)
const val TEST_DATA_LAT = 72.83312000000006
const val TEST_DATA_LNG = 18.92169000000007
const val TEST_DATA_LAT_1 = -122.084
const val TEST_DATA_LNG_1 = 37.421998333333335
const val TEST_DATA_LAT_2 = 23.013019
const val TEST_DATA_LNG_2 = 72.521230
const val TEST_DATA_LAT_3 = 23.013533
const val TEST_DATA_LNG_3 = 72.525653
const val TEST_DATA_LAT_4 = 230130.19
const val TEST_DATA_LNG_4 = 725212.30
val DISTANCE_COORDINATE_TO = LatLng(TEST_DATA_LNG, TEST_DATA_LAT) // The Taj Palace Hotel, Mumbai, Mumbai, Mahārāshtra, IND
const val AVOID_FERRIES = false
const val AVOID_TOLLS = true

const val VALID_LAT = 18.921880000000044
const val VALID_LNG = 72.83468000000005
const val INVALID_LAT = 91.0
const val INVALID_LNG = 181.0
const val TURF_TOLERANCE = 10.0

const val LAT_LNG_VALID_STRING = "$VALID_LAT, $VALID_LNG"
const val LAT_LNG_VALID_LAT_STRING = "$VALID_LAT, $INVALID_LNG"
const val LAT_LNG_VALID_LNG_STRING = "$INVALID_LAT, $VALID_LNG"
const val LAT_LNG_INVALID_STRING = "$INVALID_LAT, $INVALID_LNG"

const val ESRI = "Esri"
const val HERE = "HERE"
const val GRAB = "GrabMaps"

const val LIGHT = "Light"
const val VECTOR = "Vector"

const val SIGN_IN_SUCCESS = "Sign in Successfully"
const val SIGN_OUT_SUCCESS = "Sign out successfully"
const val NO_BROWSERS_INSTALLED = "No browsers installed."

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
const val TEST_FAILED_DUE_TO_LOGIN_NOT_SUCCESS = "Test failed due to login not success"
const val TEST_FAILED_DUE_TO_SIGN_OUT_NOT_SUCCESS = "Test failed due to sign out not success"

const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED = "Test failed due to incorrect no of providers loaded"
const val TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_ESRI = "Test failed due to incorrect style name for Esri"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI = "Test failed due to incorrect no of styles loaded for Esri"
const val TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_HERE = "Test failed due to incorrect style name for HERE"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_HERE = "Test failed due to incorrect no of styles loaded for HERE"

const val TEST_FAILED_CLIENT_NOT_INITIALIZE = "Test failed- client not initialize"
const val TEST_FAILED_COGNITO_CREDENTIALS_PROVIDER_NOT_INITIALIZE = "Test failed- cognitoCredentialsProvider not initialize"
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
const val TEST_FAILED_GRAB_MAP_NOT_ENABLE = "Grab map not enable"
const val TEST_FAILED_GRAB_MAP_NOT_SELECTED = "Grab map not selected"

const val NO_DATA_FOUND = "No data found"
const val TEST_DATA = "Mumbai"
const val TEST_DATA_1 = "en"
const val TEST_DATA_2 = "IND"
const val TEST_DATA_3 = "The Taj, Mumbai, Mahārāshtra, IND"
const val TEST_DATA_4 = "Mahārāshtra"
const val TEST_DATA_5 = "test"
const val TEST_DATA_6 = "49 sec"
const val TEST_DATA_7 = "ACTIVE"
const val TEST_DATA_8 = "jj"
const val TEST_DATA_9 = "11"

const val TEST_FAILED_DUE_TO_INVALID_LATITUDE = "Test failed due to invalid latitude"
const val TEST_FAILED_DUE_TO_INVALID_LONGITUDE = "Test failed due to invalid longitude"

const val TEST_FAILED_DUE_TO_RECEIVED_ERROR = "Test failed due to received error"
const val TEST_FAILED_DUE_TO_RECEIVED_SUCCESS = "Test failed due to received success"
const val TEST_FAILED_DUE_TO_RECEIVED_INTERNET_ERROR = "Test failed due to received internet error"
const val TEST_FAILED_DUE_TO_TIMEOUT = "Test failed due to timeout"

const val TEST_FAILED_DUE_TO_CLIENT_CONFIG_ERROR = "Test failed due to client config error"
const val TEST_FAILED_DUE_TO_SIGN_OUT_SUCCESS = "Test failed due to sign out success"
const val TEST_FAILED_DUE_TO_SIGN_OUT_ERROR = "Test failed due to sign out error"

val PLACE_TO_AMAZON_LOCATION_INPUT = Place()
    .withCountry("PRT")
    .withGeometry(
        PlaceGeometry()
            .withPoint(
                arrayListOf(-8.556649999999934, 41.17271000000005),
            ),
    )
    .withInterpolated(false)
    .withLabel("Rio Tinto, Gondomar, Porto, PRT")
    .withMunicipality("Gondomar")
    .withNeighborhood("Rio Tinto")
    .withRegion("Porto")
    .withSubRegion("Gondomar")

val PLACE_TO_AMAZON_LOCATION_OUTPUT = AmazonLocationPlace(
    coordinates = Coordinates(
        41.17271000000005,
        -8.556649999999934,
    ),
    label = "Rio Tinto, Gondomar, Porto, PRT",
    addressNumber = null,
    street = null,
    country = "PRT",
    region = "Porto",
    subRegion = "Gondomar",
    municipality = "Gondomar",
    neighborhood = "Rio Tinto",
    postalCode = null,
)

val GET_REGION_SUBREGION = "Gondomar"
val GET_REGION_REGION = "Porto"
val GET_REGION_COUNTRY = "PRT"

val GET_REGION_EMPTY = ""
val GET_REGION_SUBREGION_COUNTRY = "Gondomar, PRT"
val GET_REGION_REGION_COUNTRY = "Porto, PRT"

const val IDENTITY_POOL_ID_INVALID = "us-east-100:30f73277-588f-4587-a55a-256cc2e8a205"
const val REGION_INVALID = "us-east-2"
const val REGION_INVALID_1 = "us-east-1"
const val USER_POOL_ID_INVALID = "invalid-pool-id"
const val USER_POOL_CLIENT_ID_INVALID = "invalid-client-id"

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

const val DELAY_5000 = 5000L

const val UNIT_METRICS_M_INPUT_1 = 1000.0
const val UNIT_METRICS_M_INPUT_2 = 1001.0
const val UNIT_METRICS_M_OUTPUT_2 = "1 km"
const val UNIT_METRICS_M_OUTPUT_3 = "1,000 m"
const val UNIT_METRICS_I_INPUT_1 = 5280.0
const val UNIT_METRICS_I_INPUT_2 = 5281.0
const val UNIT_METRICS_I_OUTPUT_2 = "1 mi"
const val UNIT_METRICS_I_OUTPUT_3 = "5,280 ft"

const val UNIT_KM_TO_M_INPUT = 1.0
const val UNIT_KM_TO_M_OUTPUT = 1000.0

const val UNIT_MI_TO_FT_INPUT = 1.0
const val UNIT_MI_TO_FT_OUTPUT = 5280.0

const val UNIT_TIME_SEC_1 = 50.0
const val UNIT_TIME_SEC_2 = 120.0
const val UNIT_TIME_SEC_3 = 3600.0

const val UNIT_TIME_1_OUTPUT = "50 sec"
const val UNIT_TIME_2_OUTPUT = "2 min"
const val UNIT_TIME_3_OUTPUT = "1 hr 0 min"

const val UNIT_DEF_AWS_CONF_INPUT_POOL_ID = "us-east-1_48VeDo2Uw"
const val UNIT_DEF_AWS_CONF_INPUT_REGION = "us-east-1"

const val UNIT_DEF_AWS_CONF = "{\n" +
    "    \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
    "    \"Version\": \"0.1.0\",\n" +
    "    \"IdentityManager\": {\n" +
    "        \"Default\": {}\n" +
    "    },\n" +
    "    \"CredentialsProvider\": {\n" +
    "        \"CognitoIdentity\": {\n" +
    "            \"Default\": {\n" +
    "                \"PoolId\": \"us-east-1_48VeDo2Uw\",\n" +
    "                \"Region\": \"us-east-1\"\n" +
    "            }\n" +
    "        }\n" +
    "    }\n" +
    "}"

const val UNIT_AWS_CONF_JSON_INPUT_SCHEMA = "amazonlocationdemo"
const val UNIT_AWS_CONF_JSON_INPUT_POOL_ID = "us-east-1:30f73277-588f-4587-a55a-256cc2e8a205"
const val UNIT_AWS_CONF_JSON_INPUT_USER_POOL_ID = "us-east-1_48VeDo2Uw"
const val UNIT_AWS_CONF_JSON_INPUT_APP_CLIENT_ID = "smpc5lgho8rqlc967dja58uig"
const val UNIT_AWS_CONF_JSON_INPUT_DOMAIN = "https://973950707761.auth.us-east-1.amazoncognito.com"
const val UNIT_AWS_CONF_JSON_INPUT_REGION = "us-east-1"

const val UNIT_AWS_CONF_JSON = "{\n" +
    "    \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
    "    \"Version\": \"0.1.0\",\n" +
    "    \"IdentityManager\": {\n" +
    "        \"Default\": {}\n" +
    "    },\n" +
    "    \"CredentialsProvider\": {\n" +
    "        \"CognitoIdentity\": {\n" +
    "            \"Default\": {\n" +
    "                \"PoolId\": \"us-east-1:30f73277-588f-4587-a55a-256cc2e8a205\",\n" +
    "                \"Region\": \"us-east-1\"\n" +
    "            }\n" +
    "        }\n" +
    "    },\n" +
    "    \"CognitoUserPool\": {\n" +
    "        \"Default\": {\n" +
    "            \"PoolId\": \"us-east-1_48VeDo2Uw\",\n" +
    "            \"AppClientId\": \"smpc5lgho8rqlc967dja58uig\",\n" +
    "            \"Region\": \"us-east-1\"\n" +
    "        }\n" +
    "    },\n" +
    "    \"Auth\": {\n" +
    "        \"Default\": {\n" +
    "            \"OAuth\": {\n" +
    "              \"WebDomain\": \"https://973950707761.auth.us-east-1.amazoncognito.com\",\n" +
    "              \"AppClientId\": \"smpc5lgho8rqlc967dja58uig\",\n" +
    "              \"SignInRedirectURI\": \"amazonlocationdemo://signin/\",\n" +
    "              \"SignOutRedirectURI\": \"amazonlocationdemo://signout/\",\n" +
    "              \"Scopes\": [\n" +
    "                \"email\",\n" +
    "                \"openid\",\n" +
    "                \"profile\"\n" +
    "              ]\n" +
    "            }\n" +
    "          }\n" +
    "    }\n" +
    "}"

const val UNIT_AWS_CONF_JSON_NULL_USER_POOL_ID = "{\n" +
    "    \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
    "    \"Version\": \"0.1.0\",\n" +
    "    \"IdentityManager\": {\n" +
    "        \"Default\": {}\n" +
    "    },\n" +
    "    \"CredentialsProvider\": {\n" +
    "        \"CognitoIdentity\": {\n" +
    "            \"Default\": {\n" +
    "                \"PoolId\": \"us-east-1:30f73277-588f-4587-a55a-256cc2e8a205\",\n" +
    "                \"Region\": \"us-east-1\"\n" +
    "            }\n" +
    "        }\n" +
    "    },\n" +
    "    \"CognitoUserPool\": {\n" +
    "        \"Default\": {\n" +
    "            \"PoolId\": \"null\",\n" +
    "            \"AppClientId\": \"smpc5lgho8rqlc967dja58uig\",\n" +
    "            \"Region\": \"us-east-1\"\n" +
    "        }\n" +
    "    },\n" +
    "    \"Auth\": {\n" +
    "        \"Default\": {\n" +
    "            \"OAuth\": {\n" +
    "              \"WebDomain\": \"https://973950707761.auth.us-east-1.amazoncognito.com\",\n" +
    "              \"AppClientId\": \"smpc5lgho8rqlc967dja58uig\",\n" +
    "              \"SignInRedirectURI\": \"amazonlocationdemo://signin/\",\n" +
    "              \"SignOutRedirectURI\": \"amazonlocationdemo://signout/\",\n" +
    "              \"Scopes\": [\n" +
    "                \"email\",\n" +
    "                \"openid\",\n" +
    "                \"profile\"\n" +
    "              ]\n" +
    "            }\n" +
    "          }\n" +
    "    }\n" +
    "}"

const val UNIT_AMPLIFY_JSON_CONF_INPUT_POOL_ID = "us-east-1_48VeDo2Uw"
const val UNIT_AMPLIFY_JSON_CONF_INPUT_REGION = "us-east-1"

const val UNIT_AMPLIFY_JSON_CONFIG = "{\n" +
    "    \"UserAgent\": \"aws-amplify-cli/2.0\",\n" +
    "    \"Version\": \"1.0\",\n" +
    "    \"auth\": {\n" +
    "        \"plugins\": {\n" +
    "            \"awsCognitoAuthPlugin\": {\n" +
    "                \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
    "                \"Version\": \"0.1.0\",\n" +
    "                \"IdentityManager\": {\n" +
    "                    \"Default\": {}\n" +
    "                },\n" +
    "                \"CredentialsProvider\": {\n" +
    "                    \"CognitoIdentity\": {\n" +
    "                        \"Default\": {\n" +
    "                            \"PoolId\": \"us-east-1_48VeDo2Uw\",\n" +
    "                            \"Region\": \"us-east-1\"\n" +
    "                        }\n" +
    "                    }\n" +
    "                }\n" +
    "            }\n" +
    "        }\n" +
    "    },\n" +
    "    \"geo\": {\n" +
    "        \"plugins\": {\n" +
    "            \"awsLocationGeoPlugin\": {\n" +
    "                \"region\": \"us-east-1\",\n" +
    "                \"maps\": {\n" +
    "                    \"items\": {\n" +
    "                        \"location.aws.com.demo.maps.Esri.Light\": {\n" +
    "                            \"style\": \"VectorEsriTopographic\"\n" +
    "                        }\n" +
    "                    },\n" +
    "                    \"default\": \"location.aws.com.demo.maps.Esri.Light\"\n" +
    "                }\n" +
    "            }\n" +
    "        }\n" +
    "    }\n" +
    "}"

val LOCALE_IN = Locale("en", "IN")
val LOCALE_US = Locale.US
val METRIC = "Metric"
val IMPERIAL = "Imperial"
val AUTOMATIC = "Automatic"
val SE_REGION = "ap-southeast-1"

// Dummy credential for test case
val IDENTITY_POOL_CLIENT_ID_TEST = "66afllav6ri6hf66sikh6t6foh"
val USER_POOL_ID_TEST = "ca-central-1_X6aaHHHb6"
val SIGN_OUT_SUCCESSFULLY = "Sign out Successfully"
val SIGN_OUT_FAILED = "Sign out failed"
val SIGN_IN_SUCCESSFULLY = "Sign in Successfully"
val SIGN_IN_FAILED = "Sign in failed"
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
