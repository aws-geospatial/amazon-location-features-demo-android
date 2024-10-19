package com.aws.amazonlocation

import com.aws.amazonlocation.utils.DESCRIPTION_TAG_ESRI
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_HERE
import com.aws.amazonlocation.utils.DESCRIPTION_TAG_OPEN

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

const val DELAY_15000 = 15000L
const val DELAY_20000 = 20000L
const val DELAY_10000 = 10000L
const val DELAY_5000 = 5000L
const val DELAY_1000 = 1000L
const val DELAY_2000 = 2000L
const val DELAY_3000 = 3000L
const val DELAY_4000 = 4000L
const val SECOND_DELAY_60 = 60000L
const val DELAY_1500 = 1500L
const val WHILE_USING_THE_APP = "While using the app"
const val WHILE_USING_THE_APP_1 = "WHILE USING THE APP"
const val WHILE_USING_THE_APP_2 = "Allow only while using the app"
const val MY_LOCATION = "My Location"
const val GO = "Go"
const val ALLOW = "Allow"
const val AMAZON_MAP_READY = "Amazon Map Ready"
const val TRACKING_ENTERED = "entered"
const val TRACKING_EXITED = "exited"
const val TEST_ADDRESS = "44 Boobialla Street, Corbie Hill, Australia"
const val TEST_GEOCODE = "-31.9627092,115.9248736"
const val TEST_GEOCODE_1 = "23.0400866,72.552344"
const val TEST_WORD = "Kewdale"
const val TEST_WORD_1 = "Rio Tinto"
const val TEST_WORD_2 = "Shyamal Cross"
const val TEST_WORD_3 = "School"
const val TEST_WORD_4 = "Shyamal Cross Road"
const val TEST_WORD_5 = "auburn sydney"
const val TEST_WORD_6 = "manly beach sydney"
const val TEST_WORD_7 = "Shyamal Cross Road AMTS Bus Stand"
const val TEST_WORD_8 = "port said"
const val TEST_WORD_9 = "cloverdale perth"
const val TEST_WORD_10 = "Kewdale Perth"
const val TEST_WORD_11 = "Singapore"
const val TEST_WORD_12 = "Guoco Midtown Square"
const val TEST_WORD_13 = "Kluang "
const val TEST_WORD_14 = "dark"
const val TEST_WORD_15 = "Dark Gray"
const val TEST_IMAGE_LABEL = "Rio Tinto, Esposende, Braga, PRT"
const val TEST_IMAGE_LABEL_1 = "Rio Tinto, Gondomar, Porto, PRT"
const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"
const val TEST_FAILED = "Test failed"
const val TEST_FAILED_DIRECTION_CARD = "Test failed due to direction card not visible"
const val TEST_FAILED_SEARCH_SHEET = "Test failed due to search bottom sheet not visible"
const val TEST_FAILED_NO_SEARCH_RESULT = "Test failed due to no search result"
const val TEST_FAILED_CARD_DRIVE_GO = "Test failed due to card drive go not visible"
const val TEST_FAILED_BUTTON_DIRECTION = "Test failed due to button direction not visible"
const val TEST_FAILED_SEARCH_DIRECTION = "Test failed due to search direction not visible"
const val TEST_FAILED_ZOOM_LEVEL = "Test failed due to zoom level not available"
const val TEST_FAILED_LIST = "Test failed due to list not visible"

const val TEST_FAILED_SETTINGS_ALL_OPTIONS_NOT_VISIBLE = "Test failed due to settings all options not visible"

const val TEST_FAILED_SOURCE_NOT_ESRI = "Test failed due to default source not esri"
const val TEST_FAILED_SOURCE_NOT_HERE = "Test failed due to default source not here"
const val TEST_FAILED_INCORRECT_SOURCE = "Test failed due to incorrect source"
const val TEST_FAILED_INCORRECT_STYLE = "Test failed due to incorrect style loaded"

const val TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED = "Test failed due to default route options not loaded"

const val TEST_FAILED_NAVIGATION_TAB_SETTINGS_NOT_SELECTED = "Test failed due to navigation tab settings not selected"

const val SEARCH_TEST_WORD_1 = "Kewdale perth"
const val SEARCH_TEST_WORD_2 = "Cloverdale perth"

const val JSON_KEY_SOURCES = "sources"
const val JSON_KEY_TILES = "tiles"

const val JSON_KEY_ESRI = "esri"
const val JSON_KEY_HERE = "omv"
const val JSON_KEY_RASTER_TILES = "raster-tiles"
const val MAP_STYLE_GRAB = "GrabMaps"
const val JSON_KEY_AWS = "aws"

const val TEST_FAILED_NO_TRACKING_HISTORY_NULL = "Test failed due to no tracking history itemCount is null"
const val TEST_FAILED_NO_TRACKING_HISTORY = "Test failed due to no tracking history"
const val TEST_FAILED_NO_UPDATE_TRACKING_HISTORY = "Test failed due to tracking history not updated"

const val TEST_FAILED_STYLE_SHEET = "Test failed due to style sheet not visible"
const val TEST_FAILED_MAPBOX_NULL = "Test failed due to mapbox null"

const val TEST_FAILED_CONNECT_TO_AWS_FROM_SETTINGS = "Test failed for connect to aws from settings"
const val TEST_FAILED_CONNECT_TO_AWS_FROM_TRACKING = "Test failed for connect to aws from tracking"
const val TEST_FAILED_CONNECT_TO_AWS_FROM_GEOFENCE = "Test failed for connect to aws from geofence"
const val NESTED_SCROLL_ERROR = "Nested scroll error"

const val TEST_FAILED_SEARCH_FIELD_NOT_VISIBLE = "Test failed due to search field not visible"
const val TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE = "Test failed due to exit button not visible"
const val TEST_FAILED_DISTANCE_OR_TIME_EMPTY = "Test failed due to distance or time empty"
const val TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED = "Test failed due to zoom level not changed"
const val TEST_FAILED_ORIGIN_TEXT_NOT_MY_LOCATION = "Test failed due to origin text not my location"
const val TEST_FAILED_INVALID_IDENTITY_POOL_ID = "Test failed due to invalid identity pool id"
const val TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED = "Test failed due to location component not activated or enabled"
const val TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO = "Test failed due to count not greater than zero"
const val TEST_FAILED_MAX_ZOOM_NOT_REACHED = "Test failed due to max zoom not reached"
const val TEST_FAILED_NAVIGATION_CARD_NOT_VISIBLE = "Test failed due to navigation card not visible"
const val TEST_FAILED_HEIGHT_NOT_GREATER = "Test failed due to height not greater"
const val TEST_FAILED_NO_MATCHING_TEXT_NOT_VISIBLE = "Test failed due to no matching text not visible"
const val TEST_FAILED_COUNT_NOT_GREATER_THAN_ONE = "Test failed due to count not greater than one"
const val TEST_FAILED_COUNT_NOT_EQUAL_TO_FIVE = "Test failed due to count not equal to five"
const val TEST_FAILED_DRIVE_OR_WALK_OR_TRUCK_OPTION_NOT_VISIBLE = "Test failed due to drive or walk or truck option not visible"
const val TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT = "Test failed due to invalid origin or destination text"
const val TEST_FAILED_DIRECTION_TIME_NOT_VISIBLE = "Test failed due to direction time not visible"
const val TEST_FAILED_IMAGE_NULL = "Test failed due to image null"
const val TEST_FAILED_NOT_EQUAL = "Test failed due to not equal"
const val TEST_FAILED_POOL_ID_NOT_BLANK = "Test failed due to pool id not blank"
const val TEST_FAILED_ROUTE_OPTION_NOT_VISIBLE = "Test failed due to route option not visible"
const val TEST_FAILED_LOGOUT_BUTTON_NOT_VISIBLE = "Test failed due to logout button not visible"
const val TEST_FAILED_SIGNIN_BUTTON_NOT_VISIBLE = "Test failed due to signin button not visible"
const val TEST_FAILED_COUNT_NOT_ZERO = "Test failed due to count not zero"
const val TEST_FAILED_NOT_TRACKING_ENTERED_DIALOG = "Test failed due to not tracking entered dialog"
const val TEST_FAILED_NOT_TRACKING_EXIT_DIALOG = "Test failed due to not tracking exit dialog"
const val TEST_FAILED_LATCH_TIMEOUT = "Test failed due to latch timeout"
const val TEST_FAILED_MAP_NOT_FOUND = "Test failed due to map not found"
const val TEST_FAILED_PINCH_OUT_FAILED = "Test failed due to pinch out failed"
const val TEST_FAILED_PINCH_IN_FAILED = "Test failed due to pinch in failed"
const val TEST_FAILED_ENABLE_TRACKING = "Test failed due to enable tracking not visible"
const val TEST_FAILED_MAP_STYLE_NOT_CHANGED = "Map style not changed"
const val TEST_FAILED_NO_DATA_FOUND = "No data found"
const val TEST_FAILED_NO_MESSAGE_FOUND = "Expected message not found"

val STYLE_TAG_ESRI_1 = DESCRIPTION_TAG_ESRI + 1
val STYLE_TAG_ESRI_2 = DESCRIPTION_TAG_ESRI + 2
val STYLE_TAG_ESRI_3 = DESCRIPTION_TAG_ESRI + 3
val STYLE_TAG_ESRI_4 = DESCRIPTION_TAG_ESRI + 4
val STYLE_TAG_ESRI_5 = DESCRIPTION_TAG_ESRI + 5

val STYLE_TAG_HERE_1 = DESCRIPTION_TAG_HERE + 1
val STYLE_TAG_HERE_2 = DESCRIPTION_TAG_HERE + 2
val STYLE_TAG_HERE_3 = DESCRIPTION_TAG_HERE + 3
val STYLE_TAG_HERE_4 = DESCRIPTION_TAG_HERE + 4

val STYLE_TAG_OPEN_1 = DESCRIPTION_TAG_OPEN + 1