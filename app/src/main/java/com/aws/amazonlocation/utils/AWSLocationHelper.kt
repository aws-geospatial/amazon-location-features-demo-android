package com.aws.amazonlocation.utils

import android.location.Location
import android.os.StrictMode
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.model.CalculateRouteCarModeOptions
import aws.sdk.kotlin.services.location.model.CalculateRouteRequest
import aws.sdk.kotlin.services.location.model.CalculateRouteResponse
import aws.sdk.kotlin.services.location.model.CalculateRouteTruckModeOptions
import aws.sdk.kotlin.services.location.model.Circle
import aws.sdk.kotlin.services.location.model.DistanceUnit
import aws.sdk.kotlin.services.location.model.GeofenceGeometry
import aws.sdk.kotlin.services.location.model.GetPlaceRequest
import aws.sdk.kotlin.services.location.model.GetPlaceResponse
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import aws.sdk.kotlin.services.location.model.ListGeofencesResponse
import aws.sdk.kotlin.services.location.model.Place
import aws.sdk.kotlin.services.location.model.PlaceGeometry
import aws.sdk.kotlin.services.location.model.PutGeofenceRequest
import aws.sdk.kotlin.services.location.model.SearchForSuggestionsResult
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionRequest
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForPositionResponse
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForSuggestionsRequest
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForSuggestionsResponse
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForSuggestionsSummary
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForTextRequest
import aws.sdk.kotlin.services.location.model.SearchPlaceIndexForTextResponse
import aws.sdk.kotlin.services.location.model.TravelMode
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.epochMilliseconds
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.GeofenceCons.GEOFENCE_COLLECTION
import com.aws.amazonlocation.utils.Units.getDefaultIdentityPoolId
import com.aws.amazonlocation.utils.Units.getDistanceUnit
import com.aws.amazonlocation.utils.Units.isMetric
import com.aws.amazonlocation.utils.Units.meterToFeet
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.maplibre.android.geometry.LatLng
import software.amazon.location.auth.AuthHelper
import software.amazon.location.auth.LocationCredentialsProvider

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AWSLocationHelper(
    private var mMapHelper: MapHelper,
    private var mPreferenceManager: PreferenceManager,
) {
    private var mIdentityId: String? = null
    private var region: String? = null
    private var locationClient: LocationClient? = null
    var locationCredentialsProvider: LocationCredentialsProvider? = null
    private var credentials: aws.sdk.kotlin.services.cognitoidentity.model.Credentials? = null
    private var mBaseActivity: BaseActivity? = null
    private var apiError = "Please try again later"
    private var cognitoIdentityClient: CognitoIdentityClient? = null
    private val client = OkHttpClient()

    suspend fun initializeLocationCredentialsProvider(
        authHelper: AuthHelper,
        baseActivity: BaseActivity,
    ) {
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
            initializeAuthLocationCredentialsProvider(authHelper, baseActivity)
        } else {
            var defaultIdentityPoolId: String =
                getDefaultIdentityPoolId(
                    mPreferenceManager.getValue(
                        KEY_SELECTED_REGION,
                        regionDisplayName[0],
                    ),
                    mPreferenceManager.getValue(KEY_NEAREST_REGION, ""),
                )
            if (mAuthStatus == AuthEnum.AWS_CONNECTED.name) {
                defaultIdentityPoolId = mPreferenceManager.getValue(
                    KEY_POOL_ID,
                    "",
                ).toString()
            }
            val defaultRegion = defaultIdentityPoolId.split(":")[0]
            region = defaultRegion
            locationCredentialsProvider =
                CoroutineScope(Dispatchers.Main)
                    .async {
                        authHelper.authenticateWithCognitoIdentityPool(defaultIdentityPoolId)
                    }.await()
            locationClient = locationCredentialsProvider?.getLocationClient()
            mBaseActivity = baseActivity
            (baseActivity as MainActivity).addInterceptor()
        }
    }

    private suspend fun initializeAuthLocationCredentialsProvider(
        authHelper: AuthHelper,
        baseActivity: BaseActivity,
    ) {
        mBaseActivity = baseActivity
        try {
            val accessKey = mPreferenceManager.getValue(KEY_ACCESS_KEY, "")
            val secretKey = mPreferenceManager.getValue(KEY_SECRET_KEY, "")
            val sessionToken = mPreferenceManager.getValue(KEY_SESSION_TOKEN, "")
            val expiration = mPreferenceManager.getLongValue(KEY_EXPIRATION, 0L)
            if (accessKey.isNullOrEmpty() ||
                secretKey.isNullOrEmpty() ||
                sessionToken.isNullOrEmpty() ||
                expiration == 0L ||
                isAuthTokenExpired()
            ) {
                generateNewAuthCredentials(authHelper)
            } else {
                region = mPreferenceManager.getValue(KEY_USER_REGION, "").toString()
                credentials =
                    aws.sdk.kotlin.services.cognitoidentity.model.Credentials {
                        this.accessKeyId = accessKey
                        this.secretKey = secretKey
                        this.sessionToken = sessionToken
                        this.expiration = Instant.fromEpochMilliseconds(expiration)
                    }
                val credentialsProvider =
                    createCredentialsProvider(
                        credentials?.accessKeyId!!,
                        credentials?.secretKey!!,
                        credentials?.sessionToken!!,
                        credentials?.expiration?.epochMilliseconds!!,
                    )
                locationCredentialsProvider =
                    CoroutineScope(Dispatchers.Main)
                        .async {
                            authHelper.authenticateWithCredentialsProvider(
                                region!!,
                                credentialsProvider,
                            )
                        }.await()
                locationClient = locationCredentialsProvider?.getLocationClient()
                (baseActivity as MainActivity).addInterceptor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generateNewAuthCredentials(authHelper: AuthHelper) {
        try {
            region = mPreferenceManager.getValue(KEY_USER_REGION, "").toString()
            cognitoIdentityClient = generateCognitoIdentityClient(region)
            val identityPoolId: String =
                mPreferenceManager.getValue(KEY_POOL_ID, "").toString()
            val userPoolId: String =
                mPreferenceManager.getValue(KEY_USER_POOL_ID, "").toString()
            val idToken: String =
                mPreferenceManager.getValue(KEY_ID_TOKEN, "").toString()
            val mLogins =
                mapOf(
                    "cognito-idp.$region.amazonaws.com/$userPoolId" to idToken,
                )
            val getIdResponse =
                cognitoIdentityClient?.getId(
                    GetIdRequest {
                        this.identityPoolId = identityPoolId
                        logins = mLogins
                    },
                )
            mIdentityId =
                getIdResponse?.identityId ?: throw Exception("Failed to get identity ID")
            mPreferenceManager.setValue(KEY_IDENTITY_ID, mIdentityId!!)
            val getCredentialsResponse =
                cognitoIdentityClient?.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = mIdentityId
                        logins = mLogins
                    },
                )

            credentials = getCredentialsResponse?.credentials
            if (credentials != null) {
                credentials?.let {
                    if (it.accessKeyId == null ||
                        it.secretKey == null ||
                        it.sessionToken == null
                    ) {
                        throw Exception("Credentials generation failed")
                    }
                    mPreferenceManager.setValue(KEY_ACCESS_KEY, it.accessKeyId!!)
                    mPreferenceManager.setValue(KEY_SECRET_KEY, it.secretKey!!)
                    mPreferenceManager.setValue(KEY_SESSION_TOKEN, it.sessionToken!!)
                    mPreferenceManager.setValue(KEY_EXPIRATION, it.expiration?.epochMilliseconds!!)
                    val credentialsProvider =
                        createCredentialsProvider(
                            it.accessKeyId!!,
                            it.secretKey!!,
                            it.sessionToken!!,
                            it.expiration?.epochMilliseconds!!,
                        )
                    locationCredentialsProvider =
                        CoroutineScope(Dispatchers.Main)
                            .async {
                                authHelper.authenticateWithCredentialsProvider(
                                    region!!,
                                    credentialsProvider,
                                )
                            }.await()
                    locationClient = locationCredentialsProvider?.getLocationClient()
                    (mBaseActivity as MainActivity).addInterceptor()
                }
            } else {
                throw Exception("Credentials generation failed")
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
        }
    }

    suspend fun getAnalyticsCredentialProvider(): CredentialsProvider? {
        val defaultIdentityPoolId = BuildConfig.ANALYTICS_IDENTITY_POOL_ID
        val defaultRegion = BuildConfig.ANALYTICS_IDENTITY_POOL_ID.split(":")[0]
        return generateCredentials(defaultRegion, defaultIdentityPoolId)
    }

    private suspend fun generateCredentials(region: String, identityPoolId: String): CredentialsProvider? {
        val cognitoIdentityClient = CognitoIdentityClient { this.region = region }
        try {
            val accessKey = mPreferenceManager.getValue(KEY_ANALYTICS_ACCESS_KEY, "")
            val secretKey = mPreferenceManager.getValue(KEY_ANALYTICS_SECRET_KEY, "")
            val sessionToken = mPreferenceManager.getValue(KEY_ANALYTICS_SESSION_TOKEN, "")
            val expiration = mPreferenceManager.getLongValue(KEY_ANALYTICS_EXPIRATION, 0L)
            if (accessKey.isNullOrEmpty() ||
                secretKey.isNullOrEmpty() ||
                sessionToken.isNullOrEmpty() ||
                expiration == 0L ||
                !isAnalyticsCredentialsValid()
            ) {
                val getIdResponse = cognitoIdentityClient.getId(GetIdRequest {
                    this.identityPoolId = identityPoolId
                })
                val identityId =
                    getIdResponse.identityId ?: throw Exception("Failed to get identity ID")
                if (identityId.isNotEmpty()) {
                    val getCredentialsResponse =
                        cognitoIdentityClient.getCredentialsForIdentity(
                            GetCredentialsForIdentityRequest {
                                this.identityId = identityId
                            })

                    val credentials = getCredentialsResponse.credentials
                        ?: throw Exception("Failed to get credentials")

                    if (credentials.accessKeyId == null || credentials.secretKey == null || credentials.sessionToken == null || credentials.expiration == null) throw Exception(
                        "Credentials generation failed"
                    )
                    mPreferenceManager.setValue(KEY_ANALYTICS_ACCESS_KEY, credentials.accessKeyId!!)
                    mPreferenceManager.setValue(KEY_ANALYTICS_SECRET_KEY, credentials.secretKey!!)
                    mPreferenceManager.setValue(KEY_ANALYTICS_SESSION_TOKEN, credentials.sessionToken!!)
                    mPreferenceManager.setValue(KEY_ANALYTICS_EXPIRATION, credentials.expiration!!.epochMilliseconds)
                    return createCredentialsProvider(
                        credentials.accessKeyId!!,
                        credentials.secretKey!!,
                        credentials.sessionToken!!,
                        credentials.expiration?.epochMilliseconds!!
                    )
                } else {
                    return null
                }
            } else {
                return createCredentialsProvider(
                    accessKey,
                    secretKey,
                    sessionToken,
                    expiration
                )
            }
        } catch (e: Exception) {
            throw Exception("Credentials generation failed")
        }
    }

    fun isAnalyticsCredentialsValid(): Boolean {
        val expirationTimeMillis = mPreferenceManager.getLongValue(KEY_ANALYTICS_EXPIRATION, 0L)
        if (expirationTimeMillis == 0L) return false
        val currentTimeMillis = Instant.now().epochMilliseconds
        return currentTimeMillis < expirationTimeMillis
    }

    private fun generateCognitoIdentityClient(region: String?): CognitoIdentityClient = CognitoIdentityClient { this.region = region }

    private fun createCredentialsProvider(
        accessKeyId: String,
        secretKey: String,
        sessionToken: String,
        expiration: Long,
    ): CredentialsProvider =
        StaticCredentialsProvider(
            Credentials.invoke(
                accessKeyId = accessKeyId,
                secretAccessKey = secretKey,
                sessionToken = sessionToken,
                expiration = Instant.fromEpochMilliseconds(expiration),
            ),
        )

    fun getCredentials(): aws.sdk.kotlin.services.cognitoidentity.model.Credentials? {
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        return if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
            credentials
        } else {
            locationCredentialsProvider?.getCredentialsProvider()
        }
    }

    fun getRegion(): String? = region

    fun getIdentityId(): String? = mPreferenceManager.getValue(KEY_IDENTITY_ID, "")

    fun checkClientInitialize(): Boolean = locationClient != null

    fun checkSessionValid(activity: BaseActivity) {
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
            if (isAuthTokenExpired()) {
                activity.refreshToken()
            }
        } else {
            locationCredentialsProvider?.let {
                if (!it.isCredentialsValid()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        async { it.verifyAndRefreshCredentials() }.await()
                        locationClient = locationCredentialsProvider?.getLocationClient()
                    }
                }
            }
        }
    }

    fun isAuthTokenExpired(): Boolean {
        val expiresIn = mPreferenceManager.getLongValue(KEY_AUTH_EXPIRES_IN, 0L)
        val authFetchTime = mPreferenceManager.getLongValue(KEY_AUTH_FETCH_TIME, 0L)
        val expirationTime = authFetchTime + (expiresIn * 1000)
        val currentTime = System.currentTimeMillis()

        return currentTime > expirationTime
    }

    private suspend fun searchPlaceIndexForSuggestions(
        lat: Double?,
        lng: Double?,
        text: String,
        isGrabMapSelected: Boolean,
    ): SearchPlaceIndexForSuggestionsResponse? =
        try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_PLACE_INDEX
                    "HERE" -> HERE_PLACE_INDEX
                    "GrabMaps" -> GRAB_PLACE_INDEX
                    else -> ESRI_PLACE_INDEX
                }

            val request =
                if (isGrabMapSelected) {
                    SearchPlaceIndexForSuggestionsRequest {
                        this.text = text
                        this.language = getLanguageCode()
                        this.indexName = indexName
                        this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                    }
                } else {
                    SearchPlaceIndexForSuggestionsRequest {
                        this.text = text
                        this.language = getLanguageCode()
                        this.indexName = indexName
                        this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                        if (lng != null && lat != null) {
                            this.biasPosition = listOf(lng, lat)
                        }
                    }
                }

            withContext(Dispatchers.IO) {
                locationClient?.searchPlaceIndexForSuggestions(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
    ): CalculateRouteResponse? =
        try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_ROUTE_CALCULATOR
                    "HERE" -> HERE_ROUTE_CALCULATOR
                    "GrabMaps" -> GRAB_ROUTE_CALCULATOR
                    else -> ESRI_ROUTE_CALCULATOR
                }

            val distanceUnit =
                getDistanceUnit(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, "Automatic"))

            val request =
                when (travelMode) {
                    TravelMode.Car.value -> {
                        CalculateRouteRequest {
                            departurePosition = listOfNotNull(lngDeparture, latDeparture)
                            destinationPosition = listOfNotNull(lngDestination, latDestination)
                            carModeOptions =
                                CalculateRouteCarModeOptions {
                                    avoidTolls = isAvoidTolls
                                    avoidFerries = isAvoidFerries
                                }
                            includeLegGeometry = true
                            this.distanceUnit = DistanceUnit.fromValue(distanceUnit)
                            departNow = true
                            this.travelMode =
                                TravelMode
                                    .fromValue(travelMode)
                            calculatorName = indexName
                        }
                    }

                    TravelMode.Truck.value -> {
                        CalculateRouteRequest {
                            departurePosition = listOfNotNull(lngDeparture, latDeparture)
                            destinationPosition = listOfNotNull(lngDestination, latDestination)
                            truckModeOptions =
                                CalculateRouteTruckModeOptions {
                                    avoidTolls = isAvoidTolls
                                    avoidFerries = isAvoidFerries
                                }
                            includeLegGeometry = true
                            this.distanceUnit = DistanceUnit.fromValue(distanceUnit)
                            departNow = true
                            this.travelMode =
                                TravelMode
                                    .fromValue(travelMode)
                            calculatorName = indexName
                        }
                    }

                    else -> {
                        CalculateRouteRequest {
                            departurePosition = listOfNotNull(lngDeparture, latDeparture)
                            destinationPosition = listOfNotNull(lngDestination, latDestination)
                            includeLegGeometry = true
                            this.distanceUnit = DistanceUnit.fromValue(distanceUnit)
                            departNow = true
                            this.travelMode =
                                travelMode?.let {
                                    TravelMode.fromValue(
                                        it,
                                    )
                                }
                            calculatorName = indexName
                        }
                    }
                }

            withContext(Dispatchers.IO) {
                locationClient?.calculateRoute(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, "")
            null
        }

    suspend fun searchPlaceSuggestion(
        lat: Double?,
        lng: Double?,
        searchText: String,
        isGrabMapSelected: Boolean,
    ): SearchSuggestionResponse {
        try {
            val liveLocation = mMapHelper.getLiveLocation()
            var isLatLng = false
            val searchPlaceIndexForSuggestionsResult: SearchPlaceIndexForSuggestionsResponse?
            if (validateLatLng(searchText) != null) {
                isLatLng = true
                val mLatLng = validateLatLng(searchText)
                searchPlaceIndexForSuggestionsResult =
                    searchPlaceIndexForPosition(
                        lng = mLatLng?.longitude,
                        lat = mLatLng?.latitude,
                    )
            } else {
                searchPlaceIndexForSuggestionsResult =
                    searchPlaceIndexForSuggestions(
                        lat = lat,
                        lng = lng,
                        text = searchText,
                        isGrabMapSelected,
                    )
            }

            val mList = ArrayList<SearchSuggestionData>()
            val response =
                SearchSuggestionResponse(
                    text = searchPlaceIndexForSuggestionsResult?.summary?.text,
                    maxResults = searchPlaceIndexForSuggestionsResult?.summary?.maxResults,
                    language = searchPlaceIndexForSuggestionsResult?.summary?.language,
                    dataSource = searchPlaceIndexForSuggestionsResult?.summary?.dataSource,
                )
            if (isLatLng && searchPlaceIndexForSuggestionsResult?.results.isNullOrEmpty()) {
                addMarkerBasedOnLatLng(response, searchText, mList)
            }

            searchPlaceIndexForSuggestionsResult?.results?.forEach {
                val mSearchSuggestionData: SearchSuggestionData =
                    if (!it.placeId.isNullOrEmpty()) {
                        val getSearchResult = getPlace(it.placeId!!)
                        SearchSuggestionData(
                            placeId = it.placeId,
                            searchText = searchPlaceIndexForSuggestionsResult.summary?.text,
                            text = getSearchResult?.place?.label,
                            amazonLocationPlace = getSearchResult?.place,
                            distance =
                                getDistance(
                                    liveLocation,
                                    getSearchResult
                                        ?.place
                                        ?.geometry
                                        ?.point
                                        ?.get(1)!!,
                                    getSearchResult.place
                                        ?.geometry
                                        ?.point
                                        ?.get(0)!!,
                                ),
                        )
                    } else {
                        SearchSuggestionData(text = it.text)
                    }
                mList.add(mSearchSuggestionData)
            }
            response.data = mList
            return response
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            return SearchSuggestionResponse(
                error = apiError,
            )
        }
    }

    fun getDistance(
        liveLocation: LatLng?,
        destinationLat: Double,
        destinationLng: Double,
    ): Double? {
        var distance: Double? = null
        if (liveLocation?.latitude != null) {
            val currentLocation = Location("currentLocation")
            currentLocation.latitude = liveLocation.latitude
            currentLocation.longitude = liveLocation.longitude

            val destinationLocation = Location("destinationLocation")
            destinationLocation.latitude = destinationLat
            destinationLocation.longitude = destinationLng
            val distanceMeters = currentLocation.distanceTo(destinationLocation).toDouble()
            distance =
                if (isMetric(
                        mPreferenceManager.getValue(
                            KEY_UNIT_SYSTEM,
                            "",
                        ),
                    )
                ) {
                    distanceMeters
                } else {
                    meterToFeet(distanceMeters)
                }
        }
        return distance
    }

    private fun addMarkerBasedOnLatLng(
        mResponse: SearchSuggestionResponse,
        searchText: String,
        mList: ArrayList<SearchSuggestionData>,
    ) {
        val mLatLng = validateLatLng(searchText)
        if (mLatLng != null) {
            val place =
                Place {
                    this.geometry =
                        PlaceGeometry {
                            this.point = listOf(mLatLng.longitude, mLatLng.latitude)
                        }
                    this.label = mResponse.text
                }
            val response =
                SearchSuggestionData(
                    searchText = mResponse.text,
                    placeId = mResponse.text,
                    text = mResponse.text,
                    isPlaceIndexForPosition = true,
                    amazonLocationPlace = place,
                )
            mList.add(response)
        }
    }

    suspend fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        text: String?,
    ): SearchSuggestionResponse =
        try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_PLACE_INDEX
                    "HERE" -> HERE_PLACE_INDEX
                    "GrabMaps" -> GRAB_PLACE_INDEX
                    else -> ESRI_PLACE_INDEX
                }

            val liveLocation = mMapHelper.getLiveLocation()
            val request =
                SearchPlaceIndexForTextRequest {
                    this.indexName = indexName
                    this.text = text
                    this.language = getLanguageCode()
                    this.maxResults = SEARCH_MAX_RESULT
                    if (indexName != GRAB_PLACE_INDEX) {
                        this.biasPosition = listOfNotNull(lng, lat)
                    }
                }

            val response: SearchPlaceIndexForTextResponse? =
                withContext(Dispatchers.IO) {
                    locationClient?.searchPlaceIndexForText(request)
                }

            val searchSuggestionResponse =
                SearchSuggestionResponse(
                    text = response?.summary?.text,
                    maxResults = response?.summary?.maxResults,
                    language = response?.summary?.language,
                    dataSource = response?.summary?.dataSource,
                    error = null,
                )

            val mList = ArrayList<SearchSuggestionData>()
            if (validateLatLng(text!!) != null && response?.results?.isEmpty()!!) {
                addMarkerBasedOnLatLng(searchSuggestionResponse, text, mList)
            } else {
                response?.results?.forEach { result ->
                    val placeData =
                        SearchSuggestionData(
                            searchText = response.summary?.text,
                            amazonLocationPlace = result.place,
                            text = result.place?.label,
                            distance =
                                getDistance(
                                    liveLocation,
                                    result.place
                                        ?.geometry
                                        ?.point
                                        ?.get(1) ?: 0.0,
                                    result.place
                                        ?.geometry
                                        ?.point
                                        ?.get(0) ?: 0.0,
                                ),
                        )
                    mList.add(placeData)
                }
            }
            searchSuggestionResponse.data = mList
            searchSuggestionResponse
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            SearchSuggestionResponse(error = apiError)
        }

    suspend fun getPlace(placeId: String): GetPlaceResponse? =
        try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_PLACE_INDEX
                    "HERE" -> HERE_PLACE_INDEX
                    "GrabMaps" -> GRAB_PLACE_INDEX
                    else -> ESRI_PLACE_INDEX
                }

            val request =
                GetPlaceRequest {
                    this.indexName = indexName
                    this.placeId = placeId
                    this.language = getLanguageCode()
                }

            withContext(Dispatchers.IO) {
                locationClient?.getPlace(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun searchPlaceIndexForPosition(
        lng: Double?,
        lat: Double?,
    ): SearchPlaceIndexForSuggestionsResponse? =
        try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_PLACE_INDEX
                    "HERE" -> HERE_PLACE_INDEX
                    "GrabMaps" -> GRAB_PLACE_INDEX
                    else -> ESRI_PLACE_INDEX
                }

            val request =
                SearchPlaceIndexForPositionRequest {
                    this.indexName = indexName
                    this.language = getLanguageCode()
                    this.position = listOfNotNull(lng, lat)
                    this.maxResults = SEARCH_MAX_SUGGESTION_RESULT
                }

            val response =
                withContext(Dispatchers.IO) {
                    locationClient?.searchPlaceIndexForPosition(request)
                }

            val suggestionsResponse =
                SearchPlaceIndexForSuggestionsResponse {
                    results =
                        response?.results?.map { result ->
                            SearchForSuggestionsResult {
                                placeId = result.placeId
                                text = result.place?.label
                            }
                        }
                    summary =
                        response?.summary?.let {
                            SearchPlaceIndexForSuggestionsSummary {
                                text = "${it.position.getOrNull(1)},${it.position.getOrNull(0)}"
                                maxResults = it.maxResults
                                language = it.language
                                dataSource = it.dataSource
                            }
                        }
                }

            suggestionsResponse
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }

    suspend fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
    ): SearchPlaceIndexForPositionResponse? {
        val policy =
            StrictMode.ThreadPolicy
                .Builder()
                .permitAll()
                .build()
        StrictMode.setThreadPolicy(policy)

        return try {
            val indexName =
                when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                    "Esri" -> ESRI_PLACE_INDEX
                    "HERE" -> HERE_PLACE_INDEX
                    "GrabMaps" -> GRAB_PLACE_INDEX
                    else -> ESRI_PLACE_INDEX
                }

            val request =
                SearchPlaceIndexForPositionRequest {
                    this.indexName = indexName
                    this.language = getLanguageCode()
                    this.position = listOfNotNull(lng, lat)
                    this.maxResults = 1
                }

            withContext(Dispatchers.IO) {
                locationClient?.searchPlaceIndexForPosition(request)
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            null
        }
    }

    suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
    ): AddGeofenceResponse {
        val putGeofenceRequest =
            PutGeofenceRequest {
                this.collectionName = collectionName
                this.geofenceId = geofenceId
                geometry =
                    GeofenceGeometry {
                        circle =
                            Circle {
                                center =
                                    latLng?.let {
                                        listOf(it.longitude, it.latitude)
                                    }
                                this.radius = radius
                            }
                    }
            }

        return try {
            locationClient?.putGeofence(putGeofenceRequest)
            AddGeofenceResponse(isGeofenceDataAdded = true, errorMessage = null)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            AddGeofenceResponse(isGeofenceDataAdded = false, errorMessage = e.message)
        }
    }

    suspend fun getGeofenceList(collectionName: String): GeofenceData =
        try {
            val request =
                aws.sdk.kotlin.services.location.model.ListGeofencesRequest {
                    this.collectionName = collectionName
                }

            val response: ListGeofencesResponse? =
                withContext(Dispatchers.IO) {
                    locationClient?.listGeofences(request)
                }

            GeofenceData(
                geofenceList = ArrayList(response?.entries ?: emptyList()),
                message = null,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            GeofenceData(message = e.message)
        }

    suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
    ): DeleteGeofence {
        val batchDeleteGeofenceRequest =
            aws.sdk.kotlin.services.location.model.BatchDeleteGeofenceRequest {
                collectionName = GEOFENCE_COLLECTION
                geofenceIds = listOf(data.geofenceId)
            }

        return try {
            locationClient?.batchDeleteGeofence(batchDeleteGeofenceRequest)
            DeleteGeofence(data = data, position = position)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteGeofence(data = null, errorMessage = e.message)
        }
    }

    suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
    ): UpdateBatchLocationResponse {
        if (getIdentityId() == null) {
            UpdateBatchLocationResponse("Identity is null", false)
        }
        val map: MutableMap<String, String> =
            getIdentityId()!!.split(":").let { splitStringList ->
                mutableMapOf(
                    "region" to splitStringList[0],
                    "id" to splitStringList[1],
                )
            }

        val devicePositionUpdate =
            aws.sdk.kotlin.services.location.model.DevicePositionUpdate {
                this.position = position
                this.deviceId = deviceId
                this.sampleTime = Instant.now()
                this.positionProperties = map
            }

        val request =
            aws.sdk.kotlin.services.location.model.BatchUpdateDevicePositionRequest {
                this.trackerName = trackerName
                this.updates = listOf(devicePositionUpdate)
            }
        return try {
            locationClient?.batchUpdateDevicePosition(request)

            UpdateBatchLocationResponse(null, true)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            UpdateBatchLocationResponse(e.message, true)
        }
    }

    suspend fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        deviceId: String,
        identityId: String,
    ): UpdateBatchLocationResponse {
        val map: HashMap<String, String> = HashMap()
        identityId.split(":").let { splitStringList ->
            map["region"] = splitStringList[0]
            map["id"] = splitStringList[1]
        }

        val devicePositionUpdate =
            aws.sdk.kotlin.services.location.model.DevicePositionUpdate {
                position = position1
                this.deviceId = deviceId
                sampleTime = Instant.now()
                positionProperties = map
            }

        val request =
            aws.sdk.kotlin.services.location.model.BatchEvaluateGeofencesRequest {
                collectionName = trackerName
                devicePositionUpdates = listOf(devicePositionUpdate)
            }

        return try {
            withContext(Dispatchers.IO) {
                locationClient?.batchEvaluateGeofences(request)
            }
            UpdateBatchLocationResponse(null, true)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            UpdateBatchLocationResponse(e.message, false)
        }
    }

    suspend fun getDevicePositionHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
    ): LocationHistoryResponse {
        val request =
            aws.sdk.kotlin.services.location.model.GetDevicePositionHistoryRequest {
                this.trackerName = trackerName
                this.deviceId = deviceId
                this.startTimeInclusive = Instant.fromEpochMilliseconds(dateStart.time)
                this.endTimeExclusive = Instant.fromEpochMilliseconds(dateEnd.time)
            }
        return try {
            val response = locationClient?.getDevicePositionHistory(request)
            LocationHistoryResponse(null, response)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            LocationHistoryResponse(e.message, null)
        }
    }

    suspend fun deleteDevicePositionHistory(
        trackerName: String,
        deviceId: String,
    ): DeleteLocationHistoryResponse =
        try {
            val request =
                aws.sdk.kotlin.services.location.model.BatchDeleteDevicePositionHistoryRequest {
                    this.trackerName = trackerName
                    this.deviceIds = listOf(deviceId)
                }
            val response = locationClient?.batchDeleteDevicePositionHistory(request)
            DeleteLocationHistoryResponse(null, response)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteLocationHistoryResponse(e.message, null)
        }

    suspend fun fetchTokensWithOkHttp(
        authorizationCode: String,
    ): Response? {
        val userDomain = mPreferenceManager.getValue(KEY_USER_DOMAIN, "")
        val userPoolClientId = mPreferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        if (userDomain != null && userPoolClientId != null) {
            val redirectUri = "${mBaseActivity?.getString(R.string.AMAZON_LOCATION_SCHEMA)}://signin/"
            val tokenUrl = getTokenUrl(userDomain)
            try {
                val formBody =
                    FormBody
                        .Builder()
                        .add(KEY_REQUEST_GRANT_TYPE, AUTHORIZATION_CODE)
                        .add(KEY_REQUEST_CLIENT_ID, userPoolClientId)
                        .add(KEY_REQUEST_REDIRECT_URI, redirectUri)
                        .add(KEY_CODE, authorizationCode)
                        .build()

                val request =
                    Request
                        .Builder()
                        .url(tokenUrl)
                        .post(formBody)
                        .build()

                val response: Response = client.newCall(request).execute()
                return response
            } catch (e: IOException) {
                return null
            }
        } else {
            return null
        }
    }

    suspend fun refreshTokensWithOkHttp(): Response? {
        val userDomain = mPreferenceManager.getValue(KEY_USER_DOMAIN, "")
        val userPoolClientId = mPreferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        val refreshToken = mPreferenceManager.getValue(KEY_REFRESH_TOKEN, "")
        if (!userDomain.isNullOrEmpty() && !userPoolClientId.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            val tokenUrl = getTokenUrl(userDomain)
            try {
                val formBody =
                    FormBody
                        .Builder()
                        .add(KEY_REQUEST_GRANT_TYPE, KEY_RESPONSE_REFRESH_TOKEN)
                        .add(KEY_REQUEST_CLIENT_ID, userPoolClientId)
                        .add(KEY_RESPONSE_REFRESH_TOKEN, refreshToken)
                        .build()
                val request =
                    Request
                        .Builder()
                        .url(tokenUrl)
                        .post(formBody)
                        .build()

                val response: Response = client.newCall(request).execute()
                return response
            } catch (e: IOException) {
                return null
            }
        } else {
            return null
        }
    }

    private fun getTokenUrl(userDomain: String): String {
        return "https://$userDomain/oauth2/token"
    }
}
