package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.ChangeMultipleDepartOptionsTest
import com.aws.amazonlocation.ui.main.CheckDepartOptionsTest
import com.aws.amazonlocation.ui.main.CheckGoButtonClickLiveNavigationTest
import com.aws.amazonlocation.ui.main.CheckRouteEstimatedTimeAndDistanceTest
import com.aws.amazonlocation.ui.main.CheckRouteOptionsTest
import com.aws.amazonlocation.ui.main.CheckRouteUserEnterMyLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchLocationByGeocodeTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchNonExistingLocationTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchResultTest
import com.aws.amazonlocation.ui.main.ExploreFragmentSearchTotalResultTest
import com.aws.amazonlocation.ui.main.RouteOptionShowingTest
import com.aws.amazonlocation.ui.main.RouteReverseBetweenFormToTest
import com.aws.amazonlocation.ui.main.SearchAddressExactMatchPOICardLocationTest
import com.aws.amazonlocation.ui.main.SearchPlaceDisplayedOnMapTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExploreFragmentSearchLocationByGeocodeTest::class,
    ExploreFragmentSearchNonExistingLocationTest::class,
    SearchAddressExactMatchPOICardLocationTest::class,
    SearchPlaceDisplayedOnMapTest::class,
    ExploreFragmentSearchResultTest::class,
    ExploreFragmentSearchTotalResultTest::class,
    CheckGoButtonClickLiveNavigationTest::class,
    CheckRouteEstimatedTimeAndDistanceTest::class,
    CheckRouteOptionsTest::class,
    CheckDepartOptionsTest::class,
    ChangeMultipleDepartOptionsTest::class,
    CheckRouteUserEnterMyLocationTest::class,
    RouteOptionShowingTest::class,
    RouteReverseBetweenFormToTest::class
)
class SearchDirectionFlowSuite
