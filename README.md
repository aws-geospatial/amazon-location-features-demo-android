
# Amazon Location Services

This repository contains code for an Android application that demonstrates how to use [Amazon Location Services](https://aws.amazon.com/location/). The app showcases features like [displaying maps](https://aws.amazon.com/location/features/#Maps), searching for [points of interest](https://aws.amazon.com/location/features/#Places), calculating [routes](https://aws.amazon.com/location/features/#Routes), [geocoding](https://aws.amazon.com/location/features/#Places), [device tracking](https://aws.amazon.com/location/features/#Trackers), and creating [geofences](https://aws.amazon.com/location/features/#Geofences).

To use this code, you'll need an [AWS account](https://aws.amazon.com/) and resources to be deployed within your account. This is automated using [AWS CloudFormation](https://aws.amazon.com/cloudformation/), and this readme explains the full setup process.

Once configured, you can build and run the app locally or execute automated tests. The project includes unit and end-to-end UI tests.

The app supports the following map styles: `Standard`, `Monochrome`, `Hybrid` and `Satellite`

In addition, the app supports multiple map languages and political views, allowing users to customize the display according to their preferences or regional context.

Overall, this repo will help you get started with location-based features on Android using Amazon Location Services.

Please refer to the [AWS Geospatial repository](https://github.com/aws-geospatial/) for other demo apps, including [iOS](https://github.com/aws-geospatial/amazon-location-features-demo-ios), [React](https://github.com/aws-geospatial/amazon-location-samples-react), and [web](https://github.com/aws-geospatial/amazon-location-features-demo-web), as well as additional resources.

## Requirements

Below are the requirements for development, running and testing.

#### Development Tools

1. [Android Studio](https://developer.android.com/studio)
2. Java 11 or above.

#### Pre-requisites
1. [Create your AWS Account](https://repost.aws/knowledge-center/create-and-activate-aws-account) if needed, or sign in to your existing [AWS account](https://aws.amazon.com/)
2. Run the [AWS CloudFormation template](https://us-east-1.console.aws.amazon.com/cloudformation/home?region=us-east-1#/stacks/create?stackName=amazon-location-default-unauth-resources&templateURL=https://amazon-location-demo-resources.s3.us-west-2.amazonaws.com/default-unauth-resources-template.yaml) or use the template from `/extra/default-unauth-resources-template.yaml` to create a CloudFormation stack on AWS in the `us-east-1` region and get `IdentityPoolId`, `PinPointAppId`, `WebSocketUrl` from the stack's "outputs" tab.
    - `IdentityPoolId` value will be added to `custom.properties` file against `DEFAULT_IDENTITY_POOL_ID`.
    - `PinPointAppId` value will be added to `custom.properties` file against `ANALYTICS_APP_ID`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `SIMULATION_WEB_SOCKET_URL`.
    - Take region from IdentityPoolId (Character before ':') that value will be added to `custom.properties` file against `DEFAULT_REGION`.
3. Run the [AWS CloudFormation template](https://eu-west-1.console.aws.amazon.com/cloudformation/home?region=eu-west-1#/stacks/create?stackName=amazon-location-default-unauth-resources&templateURL=https://amazon-location-demo-resources.s3.us-west-2.amazonaws.com/default-unauth-resources-template.yaml) or use the template from `/extra/default-unauth-resources-template.yaml` to create a cloudformation stack on AWS in `eu-west-1` region and get `IdentityPoolId`, `WebSocketUrl` from stack output's tab.
    - `IdentityPoolId` value will be added to `custom.properties` file against `DEFAULT_IDENTITY_POOL_ID_EU`.
    - `WebSocketUrl` value will be added to `custom.properties` file against `SIMULATION_WEB_SOCKET_URL_EU`.
4. After adding all above details in `custom.properties` file in Android studio then open `Build -> Clean project` after this run project.

Follow this [Document](https://location.aws.com/demo/help) for detailed info to create & configure a new AWS CloudFormation template.

The required values can be found from the `Outputs` tab on your stack page created in step 2, 3, 4 and 5 above.
## Configure

Create *`custom.properties`* file inside the project root folder and add the details as below.

| KEY to be added in custom.properties | Corresponding Key from stack output                    |
|--------------------------------------|--------------------------------------------------------|
| DEFAULT_IDENTITY_POOL_ID             | IdentityPoolId                                         |
| DEFAULT_REGION                       | Take region from IdentityPoolId (Character before ':') |
| DEFAULT_IDENTITY_POOL_ID_EU          | IdentityPoolId from eu-west-1 region                   |
| API_KEY_EU_CENTRAL                   | API key from eu-west-1 region                          |
| API_KEY_US_EAST                      | API key from us-east-1 region                          |
| SIMULATION_WEB_SOCKET_URL            | Simulation WebSocketUrl                                |
| SIMULATION_WEB_SOCKET_URL_EU         | Simulation WebSocketUrl from eu-west-1 region          |
| ANALYTICS_IDENTITY_POOL_ID           | IdentityPoolId for Analytics                           |
| ANALYTICS_APP_ID                     | AnalyticsAppId                                         |

#### For Build (Required for building and running the app)

```
DEFAULT_IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
DEFAULT_REGION=xx-xxxx-x
DEFAULT_IDENTITY_POOL_ID_EU=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
API_KEY_US_EAST=xx.xxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
API_KEY_EU_CENTRAL=xx.xxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
SIMULATION_WEB_SOCKET_URL=xxxxxxxxxxxx-xxx.xxx.xx-xxxx-x.xxxxxxxxxx.com
SIMULATION_WEB_SOCKET_URL_EU=xxxxxxxxxxxx-xxx.xxx.xx-xxxx-x.xxxxxxxxxx.com
ANALYTICS_IDENTITY_POOL_ID=xx-xxxx-x:xxxx-xxxx-xxxx-xxxx
ANALYTICS_APP_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Run Locally

To run the application locally, use either an [Android Emulator](https://developer.android.com/studio/run/emulator) or a physical device connected with [USB debugging enabled](https://developer.android.com/studio/debug/dev-options#Enable-debugging).

    1. Clone the project.
    2. Open the project in Android Studio.
    3. Select 'Run Configuration' as 'app' if not already selected.
    4. Click on run button.

## Running Tests

To run tests locally remember to add the values in `secrets.properties` mentioned above in configure section.

### Unit Tests

UnitTests are configured to run with [JaCoCo](https://www.jacoco.org/jacoco/) and can be executed using various commands having different uses as below:

| Command                                         | Use                                                                                                             |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| ./gradlew testDebugUnitTest                     | Runs unit tests without JaCoCo coverage report.                                                                 |
| ./gradlew testDebugUnitTestCoverage             | Runs unit tests with JaCoCo coverage report.                                                                    |
| ./gradlew testDebugUnitTestCoverageVerification | Runs unit tests with JaCoCo code coverage report and verifies if minimum code coverage constraint is satisfied. |

The code coverage report can be found at the following path:

    app_root_dir/app/build/reports/jacoco/testDebugUnitTestCoverage


### E2E Tests

E2E tests can be executed by the following gradle commands. Test suites are configured to cover all the functionalities:

``` 
Note: 
    1. Start the emulator before executing the commands.
    2. If working on windows(Powershell/cmd) wrap the argument in double quotes.
        eg: ./gradlew app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.MapLoadAndPlaceSearchFlowSuite"
```


     ./gradlew app:connectedDebugAndroidTest -i -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.MapLoadAndPlaceSearchFlowSuite
     ./gradlew app:connectedDebugAndroidTest -i -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.SearchDirectionFlowSuite
     ./gradlew app:connectedDebugAndroidTest -i -Pandroid.testInstrumentationRunnerArguments.class=com.aws.amazonlocation.ui.MapStylesSettingAndExplorerFlowSuite

## Resources
> Geofence collections (Name)
- location.aws.com.demo.geofences.GeofenceCollection

> Trackers (Name)
- location.aws.com.demo.trackers.Tracker

## Contributing

See [CONTRIBUTING](./CONTRIBUTING.md) for more information.


## License

This library is licensed under the MIT-0 License. See [LICENSE](./LICENSE).

