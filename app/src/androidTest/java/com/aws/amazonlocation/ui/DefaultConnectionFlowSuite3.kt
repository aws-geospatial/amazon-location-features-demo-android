package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.CheckGrabGoButtonTest
import com.aws.amazonlocation.ui.main.ExploreFragmentGrabChangeStyleTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeLanguageTest
import com.aws.amazonlocation.ui.main.SettingsFragmentChangeRegionTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SettingsFragmentChangeRegionTest::class,
    SettingsFragmentChangeLanguageTest::class,
    ExploreFragmentGrabChangeStyleTest::class,
    CheckGrabGoButtonTest::class
)
class DefaultConnectionFlowSuite3
