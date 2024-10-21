package com.aws.amazonlocation.utils.analytics

enum class EventTypeEnum(
    val eventType: String,
) {
    SESSION_START("_session.start"),
    SESSION_STOP("_session.stop"),
    SESSION_END("_session.end"),
}
