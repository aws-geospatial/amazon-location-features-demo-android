package com.aws.amazonlocation

import com.aws.amazonlocation.utils.MAP

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
const val WHILE_USING_THE_APP = "While using the app"
const val WHILE_USING_THE_APP_CAPS = "WHILE USING THE APP"
const val WHILE_USING_THE_APP_ALLOW = "Allow only while using the app"
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
const val TEST_WORD_RIO_TINTO = "Rio Tinto"
const val TEST_WORD_SHYAMAL = "Shyamal Cross"
const val TEST_WORD_SCHOOL = "School"
const val TEST_WORD_SHYAMAL_CROSS_ROAD = "Shyamal Cross Road"
const val TEST_WORD_DOMINO_PIZZA_VEJALPUR = "dominos Jivraj Park Cross Road, Vejalpur Police Chowky, Vejalpur"
const val TEST_WORD_DOMINO_PIZZA = "Domino's"
const val TEST_WORD_AUBURN_SYDNEY = "auburn sydney"
const val TEST_WORD_MANLY_BEACH_SYDNEY = "manly beach sydney"
const val TEST_WORD_CLOVERDALE_PERTH = "cloverdale perth"
const val TEST_WORD_KEWDALE_PERTH = "Kewdale Perth"
const val TEST_WORD_GUOCO_MIDTOWN_SQUARE = "Guoco Midtown Square"
const val TEST_WORD_KLUANG = "Kluang "
const val TEST_WORD_ARG = "ARG"
const val TEST_WORD_RUS = "RUS"
const val TEST_WORD_LANGUAGE_AR = "العربية"
const val TEST_WORD_LANGUAGE_BO = "Bosanski"
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
const val TEST_FAILED_COUNTRY= "Test failed due to selected country doesn't match"
const val TEST_FAILED_LANGUAGE= "Test failed due to selected language doesn't match"

const val TEST_FAILED_SETTINGS_ALL_OPTIONS_NOT_VISIBLE = "Test failed due to settings all options not visible"

const val TEST_FAILED_DEFAULT_ROUTE_OPTIONS_NOT_LOADED = "Test failed due to default route options not loaded"

const val TEST_FAILED_NAVIGATION_TAB_SETTINGS_NOT_SELECTED = "Test failed due to navigation tab settings not selected"

const val SEARCH_TEST_WORD_1 = "Kewdale perth"
const val SEARCH_TEST_WORD_2 = "Cloverdale perth"


const val TEST_FAILED_NO_TRACKING_HISTORY_NULL = "Test failed due to no tracking history itemCount is null"
const val TEST_FAILED_NO_TRACKING_HISTORY = "Test failed due to no tracking history"
const val TEST_FAILED_NO_UPDATE_TRACKING_HISTORY = "Test failed due to tracking history not updated"

const val TEST_FAILED_CONNECT_TO_AWS_FROM_SETTINGS = "Test failed for connect to aws from settings"
const val NESTED_SCROLL_ERROR = "Nested scroll error"

const val TEST_FAILED_SEARCH_FIELD_NOT_VISIBLE = "Test failed due to search field not visible"
const val TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE = "Test failed due to exit button not visible"
const val TEST_FAILED_DISTANCE_OR_TIME_EMPTY = "Test failed due to distance or time empty"
const val TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED = "Test failed due to zoom level not changed"
const val TEST_FAILED_INVALID_IDENTITY_POOL_ID = "Test failed due to invalid identity pool id"
const val TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED = "Test failed due to location component not activated or enabled"
const val TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO = "Test failed due to count not greater than zero"
const val TEST_FAILED_MAX_ZOOM_NOT_REACHED = "Test failed due to max zoom not reached"
const val TEST_FAILED_NAVIGATION_CARD_NOT_VISIBLE = "Test failed due to navigation card not visible"
const val TEST_FAILED_HEIGHT_NOT_GREATER = "Test failed due to height not greater"
const val TEST_FAILED_NO_MATCHING_TEXT_NOT_VISIBLE = "Test failed due to no matching text not visible"
const val TEST_FAILED_COUNT_NOT_GREATER_THAN_ONE = "Test failed due to count not greater than one"
const val TEST_FAILED_COUNT_NOT_GREATER_THEN_TWO = "Test failed due to count not greater then 2"
const val TEST_FAILED_DRIVE_OR_WALK_OR_TRUCK_OPTION_NOT_VISIBLE = "Test failed due to drive or walk or truck option not visible"
const val TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT = "Test failed due to invalid origin or destination text"
const val TEST_FAILED_DIRECTION_TIME_NOT_VISIBLE = "Test failed due to direction time not visible"
const val TEST_FAILED_PLACE_LINK_NOT_VISIBLE = "Test failed due to place link not visible"
const val TEST_FAILED_IMAGE_NULL = "Test failed due to image null"
const val TEST_FAILED_NOT_EQUAL = "Test failed due to not equal"
const val TEST_FAILED_POOL_ID_NOT_BLANK = "Test failed due to pool id not blank"
const val TEST_FAILED_ROUTE_OPTION_NOT_VISIBLE = "Test failed due to route option not visible"
const val TEST_FAILED_LOGOUT_BUTTON_NOT_VISIBLE = "Test failed due to logout button not visible"
const val TEST_FAILED_SIGNIN_BUTTON_NOT_VISIBLE = "Test failed due to signin button not visible"
const val TEST_FAILED_COUNT_NOT_ZERO = "Test failed due to count not zero"
const val TEST_FAILED_NOT_TRACKING_ENTERED_DIALOG = "Test failed due to not tracking entered dialog"
const val TEST_FAILED_NOT_TRACKING_EXIT_DIALOG = "Test failed due to not tracking exit dialog"
const val TEST_FAILED_MAP_NOT_FOUND = "Test failed due to map not found"
const val TEST_FAILED_NO_DATA_FOUND = "No data found"
const val TEST_FAILED_NO_MESSAGE_FOUND = "Expected message not found"

val MAP_1 = MAP + 0
val MAP_2 = MAP + 1
val MAP_3 = MAP + 2
val MAP_4 = MAP + 3