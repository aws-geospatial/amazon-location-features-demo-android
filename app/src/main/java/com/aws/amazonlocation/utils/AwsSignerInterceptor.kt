package com.aws.amazonlocation.utils

import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.ui.main.MainActivity
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import software.amazon.location.auth.LocationCredentialsProvider
import software.amazon.location.auth.utils.Constants
import software.amazon.location.auth.utils.Constants.HEADER_HOST
import software.amazon.location.auth.utils.Constants.HEADER_X_AMZ_CONTENT_SHA256
import software.amazon.location.auth.utils.Constants.HEADER_X_AMZ_DATE
import software.amazon.location.auth.utils.Constants.HEADER_X_AMZ_SECURITY_TOKEN
import software.amazon.location.auth.utils.Constants.TIME_PATTERN

class AwsSignerInterceptor(
    private val serviceName: String,
    private val region: String,
    private val credentialsProvider: LocationCredentialsProvider?,
    private val mPreferenceManager: PreferenceManager,
    private val awsLocationHelper: AWSLocationHelper,
    private val activity: MainActivity
) : Interceptor {

    private val sdfMap = HashMap<String, SimpleDateFormat>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        try {
            if (!originalRequest.url.host.contains("amazonaws.com") || credentialsProvider?.getCredentialsProvider() == null) {
                return chain.proceed(originalRequest)
            }
            runBlocking {
                val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
                if (mAuthStatus != AuthEnum.SIGNED_IN.name) {
                    if (!credentialsProvider.isCredentialsValid()) {
                        credentialsProvider.verifyAndRefreshCredentials()
                    }
                } else {
                    if (awsLocationHelper.isAuthTokenExpired()) {
                        activity.refreshToken()
                    }
                }
            }
            val accessKeyId = awsLocationHelper.getCredentials()?.accessKeyId
            val secretKey = awsLocationHelper.getCredentials()?.secretKey
            val sessionToken = awsLocationHelper.getCredentials()?.sessionToken
            if (!accessKeyId.isNullOrEmpty() && !secretKey.isNullOrEmpty() && !sessionToken.isNullOrEmpty() && region.isNotEmpty()) {
                val dateMilli = Date().time
                val host = extractHostHeader(originalRequest.url.toString())
                val timeStamp = getTimeStamp(dateMilli)
                val payloadHash = sha256Hex((originalRequest.body ?: "").toString())

                val modifiedRequest =
                    originalRequest.newBuilder()
                        .header(HEADER_X_AMZ_DATE, timeStamp)
                        .header(HEADER_HOST, host)
                        .header(HEADER_X_AMZ_SECURITY_TOKEN, sessionToken)
                        .header(HEADER_X_AMZ_CONTENT_SHA256, payloadHash)
                        .build()

                val finalRequest = modifiedRequest.newBuilder()
                    .header(
                        Constants.HEADER_AUTHORIZATION,
                        modifiedRequest.awsAuthorizationHeader(
                            accessKeyId,
                            secretKey,
                            region,
                            serviceName,
                            timeStamp
                        )
                    )
                    .build()
                return chain.proceed(finalRequest)
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
        return chain.proceed(originalRequest)
    }

    private fun extractHostHeader(urlString: String): String {
        val url = URL(urlString)
        return url.host
    }

    private fun getTimeStamp(dateMilli: Long): String {
        return format(
            Date(dateMilli)
        )
    }

    private fun getSimpleDateFormat(): SimpleDateFormat {
        return sdfMap.getOrPut(TIME_PATTERN) {
            SimpleDateFormat(TIME_PATTERN, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
        }
    }

    private fun format(date: Date): String {
        return getSimpleDateFormat().format(date)
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun sha256Hex(data: String): String {
        return bytesToHex(
            MessageDigest.getInstance(HASHING_ALGORITHM)
                .digest(data.toByteArray(StandardCharsets.UTF_8))
        )
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val result = java.lang.StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02x", b))
        }
        return result.toString()
    }
}