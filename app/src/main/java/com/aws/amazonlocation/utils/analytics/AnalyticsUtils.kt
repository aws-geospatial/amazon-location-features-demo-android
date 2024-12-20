package com.aws.amazonlocation.utils.analytics

import android.os.Build
import aws.sdk.kotlin.services.pinpoint.PinpointClient
import aws.sdk.kotlin.services.pinpoint.model.EndpointDemographic
import aws.sdk.kotlin.services.pinpoint.model.EndpointLocation
import aws.sdk.kotlin.services.pinpoint.model.EndpointRequest
import aws.sdk.kotlin.services.pinpoint.model.EndpointUser
import aws.sdk.kotlin.services.pinpoint.model.Event
import aws.sdk.kotlin.services.pinpoint.model.EventsBatch
import aws.sdk.kotlin.services.pinpoint.model.EventsRequest
import aws.sdk.kotlin.services.pinpoint.model.PublicEndpoint
import aws.sdk.kotlin.services.pinpoint.model.PutEventsRequest
import aws.sdk.kotlin.services.pinpoint.model.Session
import aws.sdk.kotlin.services.pinpoint.model.UpdateEndpointRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.TimestampFormat
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.DEFAULT_COUNTRY
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_END_POINT
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.providers.LocationProvider
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AnalyticsUtils(
    private val mLocationProvider: LocationProvider,
    private val mPreferenceManager: PreferenceManager,
) {
    private var credentialProvider: CredentialsProvider?= null
    private var pinpointClient: PinpointClient? = null
    private val platformType = "Android"
    private var endpointId: String = mPreferenceManager.getValue(KEY_END_POINT, "") ?: ""
    private var userId: String = "AnonymousUser:$endpointId"
    private var session: SessionData = SessionData()

    suspend fun initAnalytics() {
        credentialProvider = mLocationProvider.getAnalyticsCredentialProvider()
        if (BuildConfig.ANALYTICS_IDENTITY_POOL_ID != "null" || credentialProvider != null) {
            val region = BuildConfig.ANALYTICS_IDENTITY_POOL_ID.split(":")[0]
            pinpointClient =
                PinpointClient {
                    this.region = region
                    credentialsProvider = credentialProvider
                }
            if (endpointId.isEmpty()) {
                endpointId = UUID.randomUUID().toString()
                mPreferenceManager.setValue(KEY_END_POINT, endpointId)
            }
        }
    }

    private suspend fun createOrUpdateEndpoint() {
        val country = if(!Locale.getDefault().country.isNullOrEmpty()) Locale.getDefault().country else DEFAULT_COUNTRY
        val endpointRequest =
            UpdateEndpointRequest {
                applicationId = BuildConfig.ANALYTICS_APP_ID
                endpointId = this@AnalyticsUtils.endpointId
                endpointRequest =
                    EndpointRequest {
                        location =
                            EndpointLocation {
                                this.country = country
                            }
                        demographic =
                            EndpointDemographic {
                                model = Build.MODEL
                                platform = "Android ($platformType)"
                            }
                        user =
                            EndpointUser {
                                userId = this@AnalyticsUtils.userId
                            }
                    }
            }
        pinpointClient?.updateEndpoint(endpointRequest)
    }

    fun recordEvent(
        event: String,
        properties: List<Pair<String, String>> = emptyList(),
    ) {
        if (BuildConfig.ANALYTICS_APP_ID == "null") return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!mLocationProvider.isUnAuthCredentialsValid(true)) {
                    runBlocking { initAnalytics() }
                }
                val events: List<EventInput> =
                    if (event == EventTypeEnum.SESSION_STOP.eventType) {
                        listOf(
                            EventInput(
                                eventType = event,
                                attributes = emptyMap(),
                                EventSession(
                                    session.id,
                                    session.startTimestamp,
                                    Instant.now().toString(),
                                ),
                            ),
                        )
                    } else {
                        listOf(EventInput(eventType = event, attributes = emptyMap()))
                    }
                val mUserId: String?
                val mAuthStatus =
                    mPreferenceManager.getValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.DEFAULT.name,
                    )
                var connectedStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_NOT_CONNECTED
                var authStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_UNAUTHENTICATED
                when (mAuthStatus) {
                    AuthEnum.SIGNED_IN.name -> {
                        mUserId = mLocationProvider.getIdentityId()
                        connectedStatus =
                            AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_CONNECTED
                        authStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_AUTHENTICATED
                    }

                    AuthEnum.AWS_CONNECTED.name -> {
                        mUserId = "AnonymousUser:$endpointId"
                        connectedStatus =
                            AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_CONNECTED
                        authStatus =
                            AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_UNAUTHENTICATED
                    }

                    else -> {
                        mUserId = "AnonymousUser:$endpointId"
                    }
                }

                if (mUserId != null && mUserId != userId) {
                    userId = mUserId
                    runBlocking { createOrUpdateEndpoint() }
                }
                val sessionStopEvent =
                    events.find { it.eventType == EventTypeEnum.SESSION_STOP.eventType }
                if (session.creationStatus == AnalyticsSessionStatus.NOT_CREATED) {
                    startSession()
                }

                val finalEvents =
                    if (sessionStopEvent != null) {
                        events +
                                EventInput(
                                    EventTypeEnum.SESSION_END.eventType,
                                    sessionStopEvent.attributes,
                                )
                    } else {
                        events
                    }
                val attributes = mutableMapOf<String, String>()
                properties.forEach { (propertyName, propertyValue) ->
                    attributes[propertyName] = propertyValue
                }
                attributes[AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS] = connectedStatus
                attributes[AnalyticsAttribute.USER_AUTHENTICATION_STATUS] = authStatus
                finalEvents.forEach {
                    it.attributes = attributes
                }
                val eventMap: Map<String, Event> =
                    finalEvents.associate {
                        UUID.randomUUID().toString() to it.toEvent()
                    }
                val batchItem = mutableMapOf<String, EventsBatch>()

                batchItem[endpointId] =
                    EventsBatch {
                        endpoint = PublicEndpoint {}
                        this.events = eventMap
                    }

                val putEventsRequest =
                    PutEventsRequest {
                        applicationId = BuildConfig.ANALYTICS_APP_ID
                        eventsRequest =
                            EventsRequest {
                                this.batchItem = batchItem
                            }
                    }
                pinpointClient?.putEvents(putEventsRequest)
                if (sessionStopEvent != null){
                    session = SessionData()
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun EventInput.toEvent(): Event =
        Event {
            eventType = this@toEvent.eventType
            timestamp = Instant.now().toString()
            attributes = this@toEvent.attributes
            session =
                Session {
                    this.id = this@AnalyticsUtils.session.id
                    this.startTimestamp = this@AnalyticsUtils.session.startTimestamp
                }
        }

    suspend fun startSession() {
        if (BuildConfig.ANALYTICS_APP_ID == "null") return
        session.creationStatus = AnalyticsSessionStatus.IN_PROGRESS
        runBlocking { createOrUpdateEndpoint() }
        session.id = UUID.randomUUID().toString()
        val iso8601Timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        session.startTimestamp = iso8601Timestamp.format(TimestampFormat.ISO_8601)
        recordEvent(EventTypeEnum.SESSION_START.eventType)
        session.creationStatus = AnalyticsSessionStatus.CREATED
    }

    fun stopSession() {
        recordEvent(
            EventTypeEnum.SESSION_STOP.eventType,
        )
    }
}
