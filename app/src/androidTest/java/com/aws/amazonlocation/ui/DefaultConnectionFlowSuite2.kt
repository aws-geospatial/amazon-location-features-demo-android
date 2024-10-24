package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.ExploreFragmentChangeStyleTest
import com.aws.amazonlocation.ui.main.ExploreFragmentPoliticalViewTest
import com.aws.amazonlocation.ui.main.SettingRouteOptionAvailableTest
import com.aws.amazonlocation.ui.main.SettingsFragmentContentTest
import com.aws.amazonlocation.ui.main.SettingsFragmentDefaultRouteTest
import com.aws.amazonlocation.ui.main.SettingsMapPoliticalViewTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SettingRouteOptionAvailableTest::class,
    SettingsFragmentContentTest::class,
    SettingsFragmentDefaultRouteTest::class,
    ExploreFragmentChangeStyleTest::class,
    ExploreFragmentPoliticalViewTest::class,
    SettingsMapPoliticalViewTest::class,
)
class DefaultConnectionFlowSuite2
