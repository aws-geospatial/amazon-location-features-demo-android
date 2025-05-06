package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
import com.aws.amazonlocation.ui.main.TrackingSignInTest
import com.aws.amazonlocation.ui.main.TrackingStartTrackingHistoryLoggedTest
import com.aws.amazonlocation.ui.main.TrackingStartTrackingMapDisplayTest
import com.aws.amazonlocation.ui.main.TrackingStartTrackingTest
import com.aws.amazonlocation.ui.main.TrackingStopTrackingTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    TrackingSignInTest::class,
    TrackingStartTrackingTest::class,
    TrackingStopTrackingTest::class,
    TrackingDeleteTrackingHistoryTest::class,
    TrackingStartTrackingHistoryLoggedTest::class,
    TrackingStartTrackingMapDisplayTest::class,
    TrackingDeleteTrackingHistoryTest::class
)
class AWSTrackingAndConnectionTestSuite
