package com.aws.amazonlocation.utils.analytics

data class EventInput(
    val eventType: String,
    var attributes: Map<String, String>,
    val session: EventSession? = null,
)