package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.ExploreFragmentChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentGrabChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentMapStyleSearchFilterTest
import com.aws.amazonlocation.ui.main.SettingRouteOptionAvailableTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeDataProviderTest
import com.aws.amazonlocation.ui.main.SettingsFragmentContentTest
import com.aws.amazonlocation.ui.main.SettingsFragmentDefaultRouteTest
import com.aws.amazonlocation.ui.main.SettingsMapStyleSearchFilterTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SettingRouteOptionAvailableTest::class,
    SettingsFragmentChangeDataProviderTest::class,
    SettingsFragmentContentTest::class,
    SettingsFragmentDefaultRouteTest::class,
    ExploreFragmentGrabChangeStyleTest::class,
    ExploreFragmentChangeStyleTest::class,
    ExploreFragmentMapStyleSearchFilterTest::class,
    SettingsMapStyleSearchFilterTest::class
)
class DefaultConnectionFlowSuite2
