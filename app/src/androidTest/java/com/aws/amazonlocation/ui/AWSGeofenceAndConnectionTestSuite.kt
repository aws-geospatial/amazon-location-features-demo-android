package com.aws.amazonlocation.ui

import com.aws.amazonlocation.ui.main.ConnectToAWSTest
import com.aws.amazonlocation.ui.main.GeofenceAddTest
import com.aws.amazonlocation.ui.main.GeofenceDeleteTest
import com.aws.amazonlocation.ui.main.GeofenceEditTest
import com.aws.amazonlocation.ui.main.SettingAWSDisconnectingTest
import com.aws.amazonlocation.ui.main.SettingSignInTest
import com.aws.amazonlocation.ui.main.SettingSignOutTest
import com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
import com.aws.amazonlocation.ui.main.TrackingGeofenceEnterTest
import com.aws.amazonlocation.ui.main.TrackingGeofenceExitTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ConnectToAWSTest::class,
    SettingSignInTest::class,
    GeofenceAddTest::class,
    TrackingGeofenceEnterTest::class,
    TrackingGeofenceExitTest::class,
    TrackingDeleteTrackingHistoryTest::class,
    GeofenceEditTest::class,
    GeofenceDeleteTest::class,
    SettingSignOutTest::class,
    SettingAWSDisconnectingTest::class,
)
class AWSGeofenceAndConnectionTestSuite
