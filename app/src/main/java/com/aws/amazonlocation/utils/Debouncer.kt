package com.aws.amazonlocation.utils

import kotlinx.coroutines.*

class Debouncer(private val coroutineScope: CoroutineScope) {
    private var debounceJob: Job? = null

    fun debounce(delayMillis: Long, action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(delayMillis)
            action()
        }
    }
}
