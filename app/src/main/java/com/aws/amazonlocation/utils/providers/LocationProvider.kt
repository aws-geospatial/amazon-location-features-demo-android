package com.aws.amazonlocation.utils.providers

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.geoplaces.GeoPlacesClient
import aws.sdk.kotlin.services.georoutes.GeoRoutesClient
import aws.sdk.kotlin.services.location.LocationClient
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.net.url.Url
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.epochMilliseconds
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.AUTHORIZATION_CODE
import com.aws.amazonlocation.utils.KEY_ACCESS_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_ACCESS_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_EXPIRATION
import com.aws.amazonlocation.utils.KEY_ANALYTICS_SECRET_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_SESSION_TOKEN
import com.aws.amazonlocation.utils.KEY_AUTH_EXPIRES_IN
import com.aws.amazonlocation.utils.KEY_AUTH_FETCH_TIME
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_CODE
import com.aws.amazonlocation.utils.KEY_EXPIRATION
import com.aws.amazonlocation.utils.KEY_IDENTITY_ID
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.KEY_REFRESH_TOKEN
import com.aws.amazonlocation.utils.KEY_REQUEST_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_REQUEST_GRANT_TYPE
import com.aws.amazonlocation.utils.KEY_REQUEST_REDIRECT_URI
import com.aws.amazonlocation.utils.KEY_RESPONSE_REFRESH_TOKEN
import com.aws.amazonlocation.utils.KEY_SECRET_KEY
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.KEY_SESSION_TOKEN
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.Units.getDefaultIdentityPoolId
import com.aws.amazonlocation.utils.regionDisplayName
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import software.amazon.location.auth.AuthHelper
import software.amazon.location.auth.LocationCredentialsProvider

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class LocationProvider(
    private var mPreferenceManager: PreferenceManager,
) {
    private var mIdentityId: String? = null
    private var region: String? = null
    private var locationClient: LocationClient? = null
    var locationCredentialsProvider: LocationCredentialsProvider? = null
    private var credentials: aws.sdk.kotlin.services.cognitoidentity.model.Credentials? = null
    private var mBaseActivity: BaseActivity? = null
    private var cognitoIdentityClient: CognitoIdentityClient? = null
    private val client = OkHttpClient()
    private var getRoutesClient: GeoRoutesClient?= null
    private var getPlaceClient: GeoPlacesClient?= null

    fun initPlaceRoutesClients() {
        val mRegion = Units.getRegion(mPreferenceManager)
        if (getRoutesClient == null) {
            getRoutesClient =
                GeoRoutesClient {
                    region = mRegion
                    credentialsProvider = createEmptyCredentialsProvider()
                }
        }
        if (getPlaceClient== null) {
            getPlaceClient =
                GeoPlacesClient {
                    region = mRegion
                    credentialsProvider = createEmptyCredentialsProvider()
                }
        }
    }

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

    fun getIdentityId(): String? = mPreferenceManager.getValue(KEY_IDENTITY_ID, "")

    fun getLocationClient(): LocationClient? = locationClient

    fun getGeoRoutesClient(): GeoRoutesClient? = getRoutesClient

    fun getGeoPlacesClient(): GeoPlacesClient? = getPlaceClient

    fun getBaseActivity(): BaseActivity? = mBaseActivity

    private fun createEmptyCredentialsProvider(): CredentialsProvider =
        StaticCredentialsProvider(
            Credentials.invoke(
                accessKeyId = "",
                secretAccessKey = "",
                sessionToken = null,
            ),
        )
}