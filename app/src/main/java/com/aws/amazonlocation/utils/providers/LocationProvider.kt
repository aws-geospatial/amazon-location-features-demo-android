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
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.epochMilliseconds
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.KEY_ACCESS_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_ACCESS_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_EXPIRATION
import com.aws.amazonlocation.utils.KEY_ANALYTICS_SECRET_KEY
import com.aws.amazonlocation.utils.KEY_ANALYTICS_SESSION_TOKEN
import com.aws.amazonlocation.utils.KEY_EXPIRATION
import com.aws.amazonlocation.utils.KEY_IDENTITY_ID
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_SECRET_KEY
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.KEY_SESSION_TOKEN
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.Units.getDefaultIdentityPoolId
import com.aws.amazonlocation.utils.regionDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import software.amazon.location.auth.AuthHelper

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class LocationProvider(
    private var mPreferenceManager: PreferenceManager
) {
    private var region: String? = null
    private var locationClient: LocationClient? = null
    private var credentials: aws.sdk.kotlin.services.cognitoidentity.model.Credentials? = null
    private var mBaseActivity: BaseActivity? = null
    private var getRoutesClient: GeoRoutesClient? = null
    private var getPlaceClient: GeoPlacesClient? = null

    suspend fun initPlaceRoutesClients(baseActivity: BaseActivity) {
        val mRegion = Units.getRegion(mPreferenceManager)
        val apiKey = Units.getApiKey(mPreferenceManager)
        if (apiKey == "null") return
        val credentialProvider =
            CoroutineScope(Dispatchers.Main)
                .async {
                    AuthHelper.withApiKey(apiKey, mRegion, baseActivity.applicationContext)
                }.await()
        if (getRoutesClient == null) {
            getRoutesClient =
                GeoRoutesClient(credentialProvider.getGeoRoutesClientConfig())
        }
        if (getPlaceClient == null) {
            getPlaceClient =
                GeoPlacesClient(credentialProvider.getGeoPlacesClientConfig())
        }
        mBaseActivity = baseActivity
    }

    suspend fun initializeLocationCredentialsProvider(baseActivity: BaseActivity) {
        val defaultIdentityPoolId: String =
            getDefaultIdentityPoolId(
                mPreferenceManager.getValue(
                    KEY_SELECTED_REGION,
                    regionDisplayName[0]
                ),
                mPreferenceManager.getValue(KEY_NEAREST_REGION, "")
            )
        if (defaultIdentityPoolId == "null") return
        val defaultRegion = defaultIdentityPoolId.split(":")[0]
        region = defaultRegion
        region?.let {
            val credentialProvider =
                CoroutineScope(Dispatchers.Main)
                    .async {
                        generateUnAuthCredentials(it, defaultIdentityPoolId, false)
                    }.await()
            locationClient = credentialProvider?.let { it1 -> generateLocationClient(it, it1) }
        }
        mBaseActivity = baseActivity
    }

    suspend fun getAnalyticsCredentialProvider(): CredentialsProvider? {
        if (BuildConfig.ANALYTICS_IDENTITY_POOL_ID == "null") return null
        val defaultIdentityPoolId = BuildConfig.ANALYTICS_IDENTITY_POOL_ID
        val defaultRegion = BuildConfig.ANALYTICS_IDENTITY_POOL_ID.split(":")[0]
        return generateUnAuthCredentials(defaultRegion, defaultIdentityPoolId, true)
    }

    private suspend fun generateUnAuthCredentials(
        region: String,
        identityPoolId: String,
        isForAnalytics: Boolean
    ): CredentialsProvider? {
        val cognitoIdentityClient = CognitoIdentityClient { this.region = region }
        try {
            val accessKey = if (isForAnalytics) {
                mPreferenceManager.getValue(
                    KEY_ANALYTICS_ACCESS_KEY,
                    ""
                )
            } else {
                mPreferenceManager.getValue(KEY_ACCESS_KEY, "")
            }
            val secretKey = if (isForAnalytics) {
                mPreferenceManager.getValue(
                    KEY_ANALYTICS_SECRET_KEY,
                    ""
                )
            } else {
                mPreferenceManager.getValue(KEY_SECRET_KEY, "")
            }
            val sessionToken = if (isForAnalytics) {
                mPreferenceManager.getValue(
                    KEY_ANALYTICS_SESSION_TOKEN,
                    ""
                )
            } else {
                mPreferenceManager.getValue(KEY_SESSION_TOKEN, "")
            }
            val expiration = if (isForAnalytics) {
                mPreferenceManager.getLongValue(
                    KEY_ANALYTICS_EXPIRATION,
                    0L
                )
            } else {
                mPreferenceManager.getLongValue(KEY_EXPIRATION, 0L)
            }
            if (accessKey.isNullOrEmpty() ||
                secretKey.isNullOrEmpty() ||
                sessionToken.isNullOrEmpty() ||
                expiration == 0L ||
                !isUnAuthCredentialsValid(isForAnalytics)
            ) {
                val getIdResponse = cognitoIdentityClient.getId(
                    GetIdRequest {
                        this.identityPoolId = identityPoolId
                    }
                )
                val identityId =
                    getIdResponse.identityId ?: throw Exception("Failed to get identity ID")

                if (identityId.isNotEmpty()) {
                    if (!isForAnalytics) mPreferenceManager.setValue(KEY_IDENTITY_ID, identityId)
                    val getCredentialsResponse =
                        cognitoIdentityClient.getCredentialsForIdentity(
                            GetCredentialsForIdentityRequest {
                                this.identityId = identityId
                            }
                        )

                    val credentials = getCredentialsResponse.credentials
                        ?: throw Exception("Failed to get credentials")

                    if (credentials.accessKeyId == null || credentials.secretKey == null || credentials.sessionToken == null || credentials.expiration == null) {
                        throw Exception(
                            "Credentials generation failed"
                        )
                    }

                    if (isForAnalytics) {
                        mPreferenceManager.setValue(
                            KEY_ANALYTICS_ACCESS_KEY,
                            credentials.accessKeyId!!
                        )
                        mPreferenceManager.setValue(
                            KEY_ANALYTICS_SECRET_KEY,
                            credentials.secretKey!!
                        )
                        mPreferenceManager.setValue(
                            KEY_ANALYTICS_SESSION_TOKEN,
                            credentials.sessionToken!!
                        )
                        mPreferenceManager.setValue(
                            KEY_ANALYTICS_EXPIRATION,
                            credentials.expiration!!.epochMilliseconds
                        )
                    } else {
                        this.credentials =
                            aws.sdk.kotlin.services.cognitoidentity.model.Credentials {
                                this.accessKeyId = credentials.accessKeyId
                                this.secretKey = credentials.secretKey
                                this.sessionToken = credentials.sessionToken
                                this.expiration = credentials.expiration
                            }
                        mPreferenceManager.setValue(KEY_ACCESS_KEY, credentials.accessKeyId!!)
                        mPreferenceManager.setValue(KEY_SECRET_KEY, credentials.secretKey!!)
                        mPreferenceManager.setValue(KEY_SESSION_TOKEN, credentials.sessionToken!!)
                        mPreferenceManager.setValue(
                            KEY_EXPIRATION,
                            credentials.expiration?.epochMilliseconds!!
                        )
                    }
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
                if (!isForAnalytics) {
                    credentials =
                        aws.sdk.kotlin.services.cognitoidentity.model.Credentials {
                            this.accessKeyId = accessKey
                            this.secretKey = secretKey
                            this.sessionToken = sessionToken
                            this.expiration = Instant.fromEpochMilliseconds(expiration)
                        }
                }
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

    fun isUnAuthCredentialsValid(isForAnalytics: Boolean): Boolean {
        val expirationTimeMillis = if (isForAnalytics) {
            mPreferenceManager.getLongValue(
                KEY_ANALYTICS_EXPIRATION,
                0L
            )
        } else {
            mPreferenceManager.getLongValue(KEY_EXPIRATION, 0L)
        }
        if (expirationTimeMillis == 0L) return false
        val currentTimeMillis = Instant.now().epochMilliseconds
        return currentTimeMillis < expirationTimeMillis
    }

    private fun createCredentialsProvider(
        accessKeyId: String,
        secretKey: String,
        sessionToken: String,
        expiration: Long
    ): CredentialsProvider =
        StaticCredentialsProvider(
            Credentials.invoke(
                accessKeyId = accessKeyId,
                secretAccessKey = secretKey,
                sessionToken = sessionToken,
                expiration = Instant.fromEpochMilliseconds(expiration)
            )
        )

    fun getCredentials(): aws.sdk.kotlin.services.cognitoidentity.model.Credentials? {
        return credentials
    }

    fun checkClientInitialize(): Boolean = locationClient != null

    fun checkSessionValid(activity: BaseActivity) {
        if (!isUnAuthCredentialsValid(false)) {
            CoroutineScope(Dispatchers.IO).launch {
                async { initializeLocationCredentialsProvider(activity) }.await()
            }
        }
    }

    private fun generateLocationClient(
        region: String,
        credentialsProvider: CredentialsProvider
    ): LocationClient {
        return LocationClient {
            this.region = region
            this.credentialsProvider = credentialsProvider
        }
    }

    fun getIdentityId(): String? = mPreferenceManager.getValue(KEY_IDENTITY_ID, "")

    fun getLocationClient(): LocationClient? = locationClient

    fun getGeoRoutesClient(): GeoRoutesClient? = getRoutesClient

    fun getGeoPlacesClient(): GeoPlacesClient? = getPlaceClient

    fun getBaseActivity(): BaseActivity? = mBaseActivity

    fun clearCredentials() {
        mPreferenceManager.removeValue(KEY_ACCESS_KEY)
        mPreferenceManager.removeValue(KEY_SECRET_KEY)
        mPreferenceManager.removeValue(KEY_SESSION_TOKEN)
        mPreferenceManager.removeValue(KEY_EXPIRATION)
    }
}
