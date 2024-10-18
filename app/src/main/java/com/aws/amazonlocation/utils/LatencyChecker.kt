package com.aws.amazonlocation.utils

import com.aws.amazonlocation.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withTimeout

class LatencyChecker(private val client: OkHttpClient = defaultClient()) {

    companion object {
        fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkLatencyForUrls(urls: List<String>): Pair<String?, Long> {
        val defaultUrl = String.format(BuildConfig.AWS_NEAREST_REGION_CHECK_URL, regionList[0])
        var fastestUrl = defaultUrl
        var minLatency: Long = Long.MAX_VALUE

        withTimeout(DELAY_LANGUAGE_3000) {
            val results = urls.map { url ->
                async(Dispatchers.IO) {
                    val start = System.currentTimeMillis()
                    client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                        if (response.isSuccessful) System.currentTimeMillis() - start else Long.MAX_VALUE
                    }
                }
            }.awaitAll()

            // Find the fastest URL
            results.forEachIndexed { index, latency ->
                if (latency < minLatency) {
                    minLatency = latency
                    fastestUrl = urls[index]
                }
            }
        }

        return fastestUrl to minLatency
    }
}
