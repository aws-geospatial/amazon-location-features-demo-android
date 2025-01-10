package com.aws.amazonlocation.utils

import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.ui.main.simulation.NotificationData
import com.google.android.gms.location.Priority

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

const val KEY_BOARD_HEIGHT = 500
const val KEY_USER_DETAILS = "user_details"
const val KEY_LOCATION_PERMISSION = "location_permission"
const val SEARCH_MAX_RESULT = 15
const val SEARCH_MAX_SUGGESTION_RESULT = 10
const val KEY_CODE = "code"
const val SIGN_OUT = "signout"
const val SIGN_IN = "signin"
const val AUTHORIZATION_CODE = "authorization_code"
const val KEY_EXPIRATION = "expiration"
const val KEY_SESSION_TOKEN = "session_token"
const val KEY_SECRET_KEY = "secret_key"
const val KEY_ACCESS_KEY = "access_key"
const val KEY_ANALYTICS_EXPIRATION = "expiration_analytics"
const val KEY_ANALYTICS_SESSION_TOKEN = "session_token_analytics"
const val KEY_ANALYTICS_SECRET_KEY = "secret_key_analytics"
const val KEY_ANALYTICS_ACCESS_KEY = "access_key_analytics"
const val KEY_IDENTITY_ID = "key_identity_id"
const val KEY_RESPONSE_ACCESS_TOKEN = "access_token"
const val KEY_RESPONSE_ID_TOKEN = "id_token"
const val KEY_RESPONSE_REFRESH_TOKEN = "refresh_token"
const val KEY_RESPONSE_EXPIRES_IN = "expires_in"
const val KEY_REQUEST_GRANT_TYPE = "grant_type"
const val KEY_REQUEST_CLIENT_ID = "client_id"
const val KEY_REQUEST_REDIRECT_URI = "redirect_uri"
const val KEY_POOL_ID = "POOL_ID"
const val KEY_USER_POOL_ID = "KEY_USER_POOL_ID"
const val KEY_USER_DOMAIN = "KEY_USER_DOMAIN"
const val KEY_USER_REGION = "KEY_USER_REGION"
const val KEY_USER_POOL_CLIENT_ID = "KEY_USER_POOL_CLIENT_ID"
const val WEB_SOCKET_URL = "web_socket_url"
const val KEY_RE_START_APP = "KEY_RE_START_APP"
const val KEY_RE_START_APP_WITH_AWS_DISCONNECT = "key_restart_app_with_aws_disconnect"
const val KEY_TAB_ENUM = "KEY_TAB_ENUM"
const val KEY_CLOUD_FORMATION_STATUS = "KEY_CLOUD_FORMATION_STATUS"
const val KEY_UNIT_SYSTEM = "KEY_UNIT_SYSTEM"
const val KEY_SELECTED_REGION = "KEY_SELECTED_REGION"
const val KEY_URL = "KEY_URL"
const val KEY_MAP_NAME = "key_map_name"
const val KEY_MAP_STYLE_NAME = "key_map_style_name"
const val KEY_COLOR_SCHEMES = "key_color_schemes"
const val KEY_POLITICAL_VIEW = "key_political_view"
const val KEY_SELECTED_MAP_LANGUAGE = "key_selected_map_language"
const val KEY_END_POINT = "key_end_point"

const val CLICK_DEBOUNCE = 1000L
const val CLICK_DEBOUNCE_ENABLE = 1200L
const val RESTART_DELAY = 800L
const val DELAY_500 = 500L
const val DELAY_300 = 300L
const val DELAY_PROCESS_1000 = 1000L
const val DELAY_SIMULATION_2000 = 2000L
const val DELAY_LANGUAGE_3000 = 3000L
const val CLICK_TIME_DIFFERENCE = 1500
const val TIME_OUT = 20000L
const val MQTT_CONNECT_TIME_OUT = 1000L
const val KEY_AVOID_TOLLS = "Avoid Tolls"
const val KEY_AVOID_FERRIES = "Avoid Ferries"
const val AWS_CLOUD_INFORMATION_FRAGMENT = "AwsCloudInformationFragment"
const val SETTING_FRAGMENT = "SettingFragment"
const val ABOUT_FRAGMENT = "AboutFragment"
const val VERSION_FRAGMENT = "VersionFragment"
const val IS_LOCATION_TRACKING_ENABLE = "is_location_tracking_enable"
const val IS_APP_FIRST_TIME_OPENED = "is_app_first_time_opened"
const val LABEL_PRE_DRAW = "_pre_draw"
const val LABEL_IN_BETWEEN = "inBetween"
const val SOURCE_SIMULATION_ICON = "source_simulation_icon"
const val LAYER_SIMULATION_ICON = "layer_simulation_icon"
const val LAYER = "layer"
const val SOURCE = "source"
const val TRACKER = "tracker"
const val ENTER = "ENTER"
const val CHANNEL_ID = "my_channel_simulation"
const val CHANNEL_NAME = "simulation Notification Channel"
const val GROUP_KEY_WORK_SIMULATION = BuildConfig.APPLICATION_ID + "SIMULATION"
const val STRING_REPLACE_KEY = "**"
const val PREFS_NAME_AUTH = "software.amazon.location.auth"
const val DEFAULT_COUNTRY = "US"

const val KEY_ID_TOKEN = "key_id_token"
const val KEY_ACCESS_TOKEN = "key_access_token"
const val KEY_REFRESH_TOKEN = "key_refresh_token"
const val KEY_AUTH_FETCH_TIME = "key_auth_fetch_time"
const val KEY_AUTH_EXPIRES_IN = "key_auth_expires_in"
const val KEY_NEAREST_REGION = "key_nearest_region"
const val IOT_POLICY = "AmazonLocationIotPolicy"
const val IOT_POLICY_UN_AUTH = "AmazonLocationIotPolicyUnauth"

const val STRING_FORMAT = "%.2f, %.2f"
const val ATTRIBUTE_LIGHT = "Light"
const val ATTRIBUTE_DARK = "Dark"

const val MAP = "MAP"
const val MILES = "miles"
const val KILOMETERS = "kilometers"

const val MAP_STYLE_ATTRIBUTION = "MapStyleAttribution"

const val simulationLatNorth = 49.295509609061924
const val simulationLonEast = -123.04870086158795
const val simulationLatSouth = 49.25908827302493
const val simulationLonWest = -123.17226119977276

const val ACCURACY = Priority.PRIORITY_HIGH_ACCURACY
const val LATENCY = 1000L
const val WAIT_FOR_ACCURATE_LOCATION = false
const val MIN_UPDATE_INTERVAL_MILLIS = 1000L

const val LANGUAGE_CODE_GERMAN = "de"
const val LANGUAGE_CODE_SPANISH = "es"
const val LANGUAGE_CODE_ENGLISH = "en"
const val LANGUAGE_CODE_FRENCH = "fr"
const val LANGUAGE_CODE_ITALIAN = "it"
const val LANGUAGE_CODE_BR_PT = "pt-BR"
const val LANGUAGE_CODE_CH_CN = "zh-CN"
const val LANGUAGE_CODE_CH_TW = "zh-TW"
const val LANGUAGE_CODE_JAPANESE = "ja"
const val LANGUAGE_CODE_KOREAN = "ko"
const val LANGUAGE_CODE_ARABIC = "ar"
const val LANGUAGE_CODE_HEBREW = "iw"
const val LANGUAGE_CODE_HINDI = "hi"
const val LANGUAGE_CODE_HEBREW_1 = "he"
const val LANGUAGE_CODE_ZH_HANT= "zh-Hant"
const val LANGUAGE_CODE_ZH= "zh"

val regionMapList: MutableMap<String, String> = mutableMapOf(
    Pair("US East (Ohio) us-east-2", "us-east-2"),
    Pair("US East (N. Virginia) us-east-1", "us-east-1"),
    Pair("US West (Oregon) us-west-2", "us-west-2"),
    Pair("Asia Pacific (Singapore) ap-southeast-1", "ap-southeast-1"),
    Pair("Asia Pacific (Sydney) ap-southeast-2", "ap-southeast-2"),
    Pair("Asia Pacific (Tokyo) ap-northeast-1", "ap-northeast-1"),
    Pair("Canada (Central) ca-central-1", "ca-central-1"),
    Pair("Europe (Frankfurt) eu-central-1", "eu-central-1"),
    Pair("Europe (Ireland) eu-west-1", "eu-west-1"),
    Pair("Europe (London) eu-west-2", "eu-west-2"),
    Pair("Europe (Stockholm) eu-north-1", "eu-north-1"),
    Pair("South America (São Paulo) sa-east-1", "sa-east-1")
)

val regionList = arrayListOf("us-east-1", "eu-west-1")
val regionDisplayName = arrayListOf("Automatic", "Europe (Ireland) eu-west-1", "US-East (N. Virginia) us-east-1")

/**
 *  * Validate Latitude and Longitude from string.
 *
 * eg 1> "Abcd" > Check LAT_LNG_REG_EXP > return false
 * eg 2> "49.281174, -123.116823" > Check LAT_LNG_REG_EXP > return true
 */
const val LAT_LNG_REG_EXP = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)"
const val attributionPattern = "<a[^>]*>|</a>"

const val GEOFENCE_NAME_REG_EXP = "^[-._\\p{L}\\p{N}]+\$"
const val userPoolIdPattern = "[\\w-]+_[0-9a-zA-Z]+"
const val userPoolClientId = "[\\w+]+"
const val regionPattern = "^[a-zA-Z-]+-\\d+$"
const val LAT_LNG_REGEX_PATTERN = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$"

const val LOCATION_AWS_PREFIX = "location.aws.com.demo."
const val LOCATION_TRACKERS_PREFIX = "trackers."
const val LOCATION_GEOFENCE_S_PREFIX = "geofences."

object Credentials {
    const val CLOUD_FORMATION_REMOVE_URL = BuildConfig.CLOUD_FORMATION_URL
    const val CLOUD_INFORMATION_LEARN_MORE = BuildConfig.BASE_DOMAIN + BuildConfig.CLOUD_FORMATION_READ_MORE_URL
}

object Distance {
    const val DISTANCE_IN_METER_30 = 30
    const val DISTANCE_IN_METER_20 = 20
    const val DISTANCE_IN_METER_10 = 10
}

object MapCameraZoom {
    const val SIMULATION_CAMERA_ZOOM_1 = 10.0
    const val NAVIGATION_CAMERA_ZOOM = 14.0
    const val DEFAULT_CAMERA_ZOOM = 14.0
    const val TRACKING_CAMERA_ZOOM = 14.0
    const val MAX_ZOOM = 22.0
    const val MIN_ZOOM = 2.0
}

object Durations {
    const val DEFAULT_INTERVAL_IN_MILLISECONDS = 500L
    const val CAMERA_DURATION_1500 = 1500
    const val CAMERA_DURATION_1000 = 1000
    const val CAMERA_BOTTOM_PADDING = 800
    const val CAMERA_TOP_RIGHT_LEFT_PADDING = 225
    const val CAMERA_RIGHT_PADDING = 180
    const val DEFAULT_RADIUS = 80
    const val DELAY_FOR_FRAGMENT_LOAD = 500L
    const val DELAY_FOR_BOTTOM_SHEET_LOAD = 500L
}

object GeofenceCons {
    const val GEOFENCE_COLLECTION = LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "GeofenceCollection"
    const val CIRCLE_CENTER_SOURCE_ID = "CIRCLE_CENTER_SOURCE_ID"
    const val CIRCLE_CENTER_ICON_ID = "CIRCLE_CENTER_ICON_ID"
    const val CIRCLE_CENTER_LAYER_ID = "CIRCLE_CENTER_LAYER_ID"
    const val TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID =
        "TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID"
    const val TURF_CALCULATION_FILL_LAYER_ID = "TURF_CALCULATION_FILL_LAYER_ID"

    const val TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID =
        "TURF_CALCULATION_LINE_LAYER_GEOJSON_SOURCE_ID"
    const val TURF_CALCULATION_LINE_LAYER_ID = "TURF_CALCULATION_LINE_LAYER_ID"
    const val RADIUS_SEEKBAR_MAX = 10000
    const val RADIUS_SEEKBAR_DIFFERENCE = 1

    const val CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID = "CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID"
    const val CIRCLE_DRAGGABLE_VISIBLE_ICON_ID = "CIRCLE_DRAGGABLE_VISIBLE_ICON_ID"
    const val CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID = "CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID"

    const val CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID = "CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID"

    const val CIRCLE_DRAGGABLE_BEARING = 90.0

    const val GEOFENCE_MIN_RADIUS = 10
}

object TrackerCons {
    const val TRACKER_COLLECTION = LOCATION_AWS_PREFIX + LOCATION_TRACKERS_PREFIX + "Tracker"
}

object DateFormat {
    const val MMM_DD_YYYY = "MMM dd, yyyy"
    const val HH_MM_AA = "HH:mm aa"
}

val simulationCollectionName = arrayListOf(
    LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "BusStopsCollection01",
    LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "BusStopsCollection02",
    LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "BusStopsCollection03",
    LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "BusStopsCollection04",
    LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "BusStopsCollection05"
)

val notificationData = arrayListOf(
    NotificationData("Bus 01 Robson", false),
    NotificationData("Bus 02 Davie", false),
    NotificationData("Bus 03 Victoria", false),
    NotificationData("Bus 04 Knight", false),
    NotificationData("Bus 05 UBC", false)
)

val requiredFields = mapOf(
    "API_KEY_EU_CENTRAL" to BuildConfig.API_KEY_EU_CENTRAL,
    "API_KEY_US_EAST" to BuildConfig.API_KEY_US_EAST
)

val simulationFields = mapOf(
    "DEFAULT_IDENTITY_POOL_ID" to BuildConfig.DEFAULT_IDENTITY_POOL_ID,
    "DEFAULT_IDENTITY_POOL_ID_EU" to BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU,
    "DEFAULT_REGION" to BuildConfig.DEFAULT_REGION,
    "SIMULATION_WEB_SOCKET_URL" to BuildConfig.SIMULATION_WEB_SOCKET_URL,
    "SIMULATION_WEB_SOCKET_URL_EU" to BuildConfig.SIMULATION_WEB_SOCKET_URL_EU,
)

val analyticsFields = mapOf(
    "ANALYTICS_IDENTITY_POOL_ID" to BuildConfig.ANALYTICS_IDENTITY_POOL_ID,
    "ANALYTICS_APP_ID" to BuildConfig.ANALYTICS_APP_ID
)

object EventType {
    const val SCREEN_OPEN = "SCREEN_OPEN"
    const val SCREEN_CLOSE = "SCREEN_CLOSE"

    const val APPLICATION_ERROR = "APPLICATION_ERROR"

    const val MAP_STYLE_CHANGE = "MAP_STYLE_CHANGE"
    const val PLACE_SEARCH = "PLACES_SEARCH"
    const val ROUTE_SEARCH = "ROUTE_SEARCH"
    const val ROUTE_OPTION_CHANGED = "ROUTE_OPTION_CHANGED"
    const val MAP_UNIT_CHANGE = "MAP_UNIT_CHANGE"

    // User
    const val SIGN_IN_STARTED = "SIGN_IN_STARTED"
    const val SIGN_IN_SUCCESSFUL = "SIGN_IN_SUCCESSFUL"
    const val SIGN_IN_FAILED = "SIGN_IN_FAILED"
    const val SIGN_OUT_SUCCESSFUL = "SIGN_OUT_SUCCESSFUL"
    const val SIGN_OUT_FAILED = "SIGN_OUT_FAILED"
    const val AWS_ACCOUNT_CONNECTION_STARTED = "AWS_ACCOUNT_CONNECTION_STARTED"
    const val AWS_ACCOUNT_CONNECTION_SUCCESSFUL = "AWS_ACCOUNT_CONNECTION_SUCCESSFUL"
    const val AWS_ACCOUNT_CONNECTION_FAILED = "AWS_ACCOUNT_CONNECTION_FAILED"
    const val AWS_ACCOUNT_CONNECTION_STOPPED = "AWS_ACCOUNT_CONNECTION_STOPPED"

    // Tracker and Geofence
    const val GEOFENCE_CREATION_STARTED = "GEOFENCE_CREATION_STARTED"
    const val GEOFENCE_CREATION_SUCCESSFUL = "GEOFENCE_CREATION_SUCCESSFUL"
    const val GEOFENCE_CREATION_FAILED = "GEOFENCE_CREATION_FAILED"
    const val GEOFENCE_DELETION_SUCCESSFUL = "GEOFENCE_DELETION_SUCCESSFUL"
    const val GEOFENCE_DELETION_FAILED = "GEOFENCE_DELETION_FAILED"
    const val GET_GEOFENCES_LIST_SUCCESSFUL = "GET_GEOFENCES_LIST_SUCCESSFUL"
    const val GET_GEOFENCES_LIST_FAILED = "GET_GEOFENCES_LIST_FAILED"
    const val GEO_EVENT_TRIGGERED = "GEO_EVENT_TRIGGERED"
    const val GEOFENCE_ITEM_SELECTED = "GEOFENCE_ITEM_SELECTED"
    const val TRACKER_SAVED = "TRACKER_SAVED"
    const val START_TRACKING = "START_TRACKING"
    const val STOP_TRACKING = "STOP_TRACKING"
    const val CHANGE_BUS_TRACKING_HISTORY = "CHANGE_BUS_TRACKING_HISTORY"
    const val ENABLE_NOTIFICATION = "ENABLE_NOTIFICATION"
    const val DISABLE_NOTIFICATION = "DISABLE_NOTIFICATION"
    const val START_SIMULATION = "START_SIMULATION"

    // General
    const val LANGUAGE_CHANGED = "LANGUAGE_CHANGED"
}

object AnalyticsAttribute {
    const val USER_AWS_ACCOUNT_CONNECTION_STATUS = "userAWSAccountConnectionStatus"
    const val USER_AUTHENTICATION_STATUS = "userAuthenticationStatus"
    const val USER_AWS_ACCOUNT_CONNECTION_STATUS_NOT_CONNECTED = "Not connected"
    const val USER_AWS_ACCOUNT_CONNECTION_STATUS_CONNECTED = "Connected"
    const val USER_AWS_ACCOUNT_CONNECTION_STATUS_UNAUTHENTICATED = "Unauthenticated"
    const val USER_AWS_ACCOUNT_CONNECTION_STATUS_AUTHENTICATED = "Authenticated"
    const val SCREEN_NAME = "screenName"
    const val TRAVEL_MODE = "travelMode"
    const val DISTANCE_UNIT = "distanceUnit"
    const val TRIGGERED_BY = "triggeredBy"
    const val ID = "id"
    const val PROVIDER = "provider"
    const val VALUE = "value"
    const val TYPE = "type"
    const val ACTION = "action"
    const val AVOID_FERRIES = "AvoidFerries"
    const val AVOID_TOLLS = "AvoidTolls"
    const val GEOFENCE_ID = "geofenceId"
    const val EVENT_TYPE = "eventType"
    const val ERROR = "error"
    const val NUMBER_OF_TRACKER_POINTS = "numberOfTrackerPoints"
    const val BUS_NAME = "busName"
    const val LANGUAGE = "language"
}
object AnalyticsAttributeValue {
    const val EXPLORER = "Explorer"
    const val TRACKERS = "Trackers"
    const val GEOFENCES = "Geofences"
    const val SETTINGS = "Settings"
    const val ABOUT = "About"
    const val SIMULATION = "Simulation"
    const val UNITS = "Units"
    const val MAP_STYLE = "Map style"
    const val LANGUAGES = "Languages"
    const val DEFAULT_ROUTE_OPTIONS = "Default route options"
    const val CONNECT_YOUR_AWS_ACCOUNT = "Connect your AWS account"
    const val ATTRIBUTION = "Attribution"
    const val VERSION = "Version"
    const val TERMS_CONDITIONS = "Terms & Conditions"
    const val HELP = "Help"
    const val PLACES_POPUP = "PLACES_POPUP"
    const val COORDINATES = "Coordinates"
    const val TEXT = "Text"
    const val AUTOCOMPLETE = "Autocomplete"
    const val ROUTE_MODULE = "ROUTE_MODULE"
    const val TO_SEARCH_AUTOCOMPLETE = "To search autocomplete"
    const val FROM_SEARCH_AUTOCOMPLETE = "From search autocomplete"
    const val ENTER = "Enter"
    const val EXIT = "Exit"
}
