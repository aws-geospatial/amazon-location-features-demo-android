package com.aws.amazonlocation.utils.analytics

data class SessionData(
    var id: String? = null,
    var startTimestamp: String? = null,
    var creationStatus: AnalyticsSessionStatus = AnalyticsSessionStatus.NOT_CREATED,
)