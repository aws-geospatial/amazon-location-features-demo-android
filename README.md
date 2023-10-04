
# Amazon Location Services

Android application for using Location Services of Amazon.

Features like map, searching points of interest, calculating routes, geocode, tracking devices, and triggering geofences using Amazon Location Service.
## Requirements

Below are the requirements for development, running and testing.

#### Development Tools

1. Android Studio
2. Java 11 or above.

#### Pre-requisites

1. Sign in to [Amazon AWS account](https://aws.amazon.com/)
2. Run the [CF template](https://us-west-2.console.aws.amazon.com/cloudformation/home?region=us-west-2#/stacks/create?stackName=amazon-location-resources-setup&templateURL=https://amazon-location-resources-setup.s3.amazonaws.com/location-services.yaml) using your own AWS account and get `IdentityPoolId` and `region` from stack output

Follow this [Document](https://location.aws.com/demo/help) for detailed info to create & configure a new Cloud formation.

The required values can be found from the `Outputs` tab on your stack page created in step 2 above.
## Configure

Create *`custom.properties`* file inside the project root folder and add the details as below.

| KEY to be added in custom.properties | Corresponding Key from stack output                    |
|--------------------------------------|--------------------------------------------------------|
| DEFAULT_IDENTITY_POOL_ID             | IdentityPoolId                                         |
| DEFAULT_REGION                       | Take region from IdentityPoolId (Character before ':') |
| DEFAULT_IDENTITY_POOL_ID_EU          | IdentityPoolId form eu-west-1 region                   |
| DEFAULT_IDENTITY_POOL_ID_AP          | IdentityPoolId form ap-southeast-1 region              |
| SIMULATION_WEB_SOCKET_URL            | Simulation WebSocketUrl                                |
| SIMULATION_WEB_SOCKET_URL_EU         | Simulation WebSocketUrl form eu-west-1 region          |
| SIMULATION_WEB_SOCKET_URL_AP         | Simulation WebSocketUrl form ap-southeast-1 region     |
| ANALYTICS_APP_ID                     | AnalyticsAppId                                         |
| IDENTITY_POOL_ID                     | IdentityPoolId                                         |
| USER_DOMAIN                          | UserDomain                                             |
| USER_POOL_CLIENT_ID                  | UserPoolClientId                                       |
| USER_POOL_ID                         | UserPoolId                                             |
| WEB_SOCKET_URL                       | WebSocketUrl                                           |

#### For Build (Required for building and running the app)

```
DEFAULT_IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_REGION=xx-xxxx-x
DEFAULT_IDENTITY_POOL_ID_EU=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_IDENTITY_POOL_ID_AP=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
```

#### optional values to add after above if you want to run tests locally. (This can be a different stack only for testing)

```
IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
USER_DOMAIN=https://xxxxxxxxxxxx.xxxx.xx-xxxx-x.amazoncognito.com
USER_POOL_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
USER_POOL_ID=xx-xxxx-x_xxxxxxxxx
WEB_SOCKET_URL=xx....x-xxx.iot.xx-xxxx-x.amazonaws.com
USER_LOGIN_NAME=<aws username>
USER_LOGIN_PASSWORD=<aws password>
```

## Run Locally

To run the application locally either an emulator needs to be running or a physical device connected with usb debugging enable.

    1. Clone the project.
    2. Open the project in android studio.
    3. Select 'Run Configuration' as 'app' if not already selected.
    4. Click on run button.


## Running Tests

To run tests locally remember to add the values in `secrets.properties` mentioned above in configure section.

### Unit Tests

UnitTests are configured to run with jacoco and can be executed using various commands having different uses as below:

| Command                                         | Use                                                                                                             |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| ./gradlew testDebugUnitTest                     | Runs unit tests without jacoco coverage report.                                                                 |
| ./gradlew testDebugUnitTestCoverage             | Runs unit tests with jacoco coverage report.                                                                    |
| ./gradlew testDebugUnitTestCoverageVerification | Runs unit tests with jacoco code coverage report and verifies if minimum code coverage constraint is satisfied. |

The code coverage report can be found at the following path:

    app_root_dir/app/build/reports/jacoco/testDebugUnitTestCoverage


### E2E Tests

E2E tests can be executed by the following gradle commands. Test suites are configured to cover all the functionalities:

``` 
Note: 
    1. Start the emulator before executing the commands.
    2. If working on windows(Powershell/cmd) wrap the argument in double quotes.
        eg: ./gradlew app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite"
```


    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite2
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SimulationStartTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SimulationStopTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.DefaultConnectionFlowSuite3
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.AWSConnectionSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.AWSSignInSuite
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStopTrackingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingHistoryLoggedTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingStartTrackingMapDisplayTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceAddTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingGeofenceEnterTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingGeofenceExitTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.TrackingDeleteTrackingHistoryTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceEditTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.GeofenceDeleteTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingSignOutTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingSignInTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.SettingAWSDisconnectingTest
    ./gradlew app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.main.ConnectToAWSTest
## Resources
> Maps (Name - Style)
- location.aws.com.demo.maps.Esri.DarkGrayCanvas - VectorEsriDarkGrayCanvas
- location.aws.com.demo.maps.Esri.Imagery - RasterEsriImagery
- location.aws.com.demo.maps.Esri.Light - VectorEsriTopographic
- location.aws.com.demo.maps.Esri.LightGrayCanvas - VectorEsriLightGrayCanvas
- location.aws.com.demo.maps.Esri.Navigation - VectorEsriNavigation
- location.aws.com.demo.maps.Esri.Streets - VectorEsriStreets
- location.aws.com.demo.maps.HERE.Explore - VectorHereExplore
- location.aws.com.demo.maps.HERE.Contrast - VectorHereContrast
- location.aws.com.demo.maps.HERE.ExploreTruck - VectorHereExploreTruck
- location.aws.com.demo.maps.HERE.Hybrid - HybridHereExploreSatellite
- location.aws.com.demo.maps.HERE.Imagery - RasterHereExploreSatellite
- location.aws.com.demo.maps.Grab.StandardLight - VectorGrabStandardLight
- location.aws.com.demo.maps.Grab.StandardDark - VectorGrabStandardDark

> Place indexes (Name)
- location.aws.com.demo.places.Esri.PlaceIndex
- location.aws.com.demo.places.HERE.PlaceIndex
- location.aws.com.demo.places.Grab.PlaceIndex

> Route calculators (Name)
- location.aws.com.demo.routes.Esri.RouteCalculator
- location.aws.com.demo.routes.HERE.RouteCalculator
- location.aws.com.demo.routes.Grab.RouteCalculator

> Geofence collections (Name)
- location.aws.com.demo.geofences.GeofenceCollection

> Trackers (Name)
- location.aws.com.demo.trackers.Tracker
## Contributing

See [CONTRIBUTING](./CONTRIBUTING.md) for more information.


## License

This library is licensed under the MIT-0 License. See [LICENSE](./LICENSE).

