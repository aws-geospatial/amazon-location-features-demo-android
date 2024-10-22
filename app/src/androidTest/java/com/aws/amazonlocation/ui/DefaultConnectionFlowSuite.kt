package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.AfterSearchDirectionButtonWorkingTest
import com.aws.amazonlocation.ui.main.CheckGoButtonClickLiveNavigationTest
import com.aws.amazonlocation.ui.main.CheckRouteEstimatedTimeAndDistanceTest
import com.aws.amazonlocation.ui.main.CheckRouteMapAdjustedTest
import com.aws.amazonlocation.ui.main.CheckRouteOptionsTest
import com.aws.amazonlocation.ui.main.CheckRouteUserEnterMyLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentEnableLocation
import com.aws.amazonlocation.ui.main.ExploreFragmentGrabChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentLiveNavigationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapCurrentLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapFunctionWithoutAwsLoginTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapLoadingTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapLocateMeButtonTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapStyleSearchFilterTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapZoomInOutTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMaxZoomInOutTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchByCategoriesTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchCollapseTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchExistsTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchGeocodeReversedTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchLocationByAddressTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchLocationByGeocodeTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchNonExistingLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchResultTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchTotalResultTest
import com.aws.amazonlocation.ui.main.RouteOptionShowingTest
import com.aws.amazonlocation.ui.main.RouteReverseBetweenFormToTest
import com.aws.amazonlocation.ui.main.SearchAddressExactMatchPOICardLocationTest
import com.aws.amazonlocation.ui.main.SearchPlaceDisplayedOnMapPOICircleTest
import com.aws.amazonlocation.ui.main.SearchPlaceDisplayedOnMapTest
import com.aws.amazonlocation.ui.main.SearchResultComparisonTest
import com.aws.amazonlocation.ui.main.SettingRouteOptionAvailableTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeDataProviderTest
import com.aws.amazonlocation.ui.main.SettingsFragmentContentTest
import com.aws.amazonlocation.ui.main.SettingsFragmentDefaultRouteTest
import com.aws.amazonlocation.ui.main.SettingsMapStyleSearchFilterTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExploreFragmentEnableLocation::class,
    ExploreFragmentMapLoadingTest::class,
    ExploreFragmentMapCurrentLocationTest::class,
    ExploreFragmentLiveNavigationTest::class,
    ExploreFragmentMapFunctionWithoutAwsLoginTest::class,
    ExploreFragmentMapLocateMeButtonTest::class,
    ExploreFragmentMapZoomInOutTest::class,
    ExploreFragmentMaxZoomInOutTest::class,
    AfterSearchDirectionButtonWorkingTest::class,
    ExploreFragmentSearchByCategoriesTest::class,
    ExploreFragmentSearchCollapseTest::class,
    ExploreFragmentSearchExistsTest::class,
    ExploreFragmentSearchGeocodeReversedTest::class,
    ExploreFragmentSearchLocationByAddressTest::class,
    ExploreFragmentSearchLocationByGeocodeTest::class,
    ExploreFragmentSearchNonExistingLocationTest::class,
    SearchPlaceDisplayedOnMapPOICircleTest::class,
    SearchAddressExactMatchPOICardLocationTest::class,
    SearchPlaceDisplayedOnMapTest::class,
    SearchResultComparisonTest::class,
    ExploreFragmentSearchResultTest::class,
    ExploreFragmentSearchTotalResultTest::class,
    CheckGoButtonClickLiveNavigationTest::class,
    CheckRouteEstimatedTimeAndDistanceTest::class,
    CheckRouteMapAdjustedTest::class,
    CheckRouteOptionsTest::class,
    CheckRouteUserEnterMyLocationTest::class,
    RouteOptionShowingTest::class,
    RouteReverseBetweenFormToTest::class
)
class DefaultConnectionFlowSuite
