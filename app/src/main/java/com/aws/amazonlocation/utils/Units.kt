package com.aws.amazonlocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.location.LocationManager
import android.provider.Settings
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.RouteSimulationData
import com.google.gson.Gson
import java.io.InputStream
import java.io.InputStreamReader
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
object Units {

    fun getMetricsNew(context: Context, distance: Double, isMetric: Boolean): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
        }

        return if (isMetric) {
            "${formatter.format(distance / 1000)} ${context.getString(R.string.label_km)}"
        } else {
            "${formatter.format(distance / 5280)} ${context.getString(R.string.label_mi)}"
        }
    }

    fun getMetrics(context: Context, distance: Double, isMetric: Boolean): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = if (isMetric) 1 else 2
        }
        return if (isMetric) {
            if (distance <= 1000) {
                "${formatter.format(distance)} ${context.getString(R.string.label_m)}"
            } else {
                "${formatter.format(distance / 1000)} ${context.getString(R.string.label_km)}"
            }
        } else {
            if (distance <= 5280) {
                "${formatter.format(distance)} ${context.getString(R.string.label_ft)}"
            } else {
                "${formatter.format(distance / 5280)} ${context.getString(R.string.label_mi)}"
            }
        }
    }

    fun convertToLowerUnit(distance: Double, isMetric: Boolean): Double {
        return if (isMetric) {
            kiloMeterToMeter(distance)
        } else {
            milesToFeet(distance)
        }
    }

    fun kiloMeterToMeter(kiloMeter: Double): Double {
        return kiloMeter * 1000
    }

    fun milesToFeet(miles: Double): Double {
        return miles * 5280
    }

    fun meterToFeet(meter: Double): Double {
        return meter * 3.2808399
    }

    fun getTime(context: Context, second: Double): String {
        val mSeconds = second.toInt().toLong()
        TimeUnit.SECONDS.toDays(mSeconds).toInt()
        val mHours: Long =
            TimeUnit.SECONDS.toHours(mSeconds)
        val mMinute: Long =
            TimeUnit.SECONDS.toMinutes(mSeconds) - TimeUnit.SECONDS.toHours(mSeconds) * 60
        val mSecondNew: Long =
            TimeUnit.SECONDS.toSeconds(mSeconds) - TimeUnit.SECONDS.toMinutes(mSeconds) * 60

        var mTime = if (mMinute == 0L && mHours == 0L) {
            buildString {
                append(mSecondNew)
                append(" ")
                append(context.getString(R.string.label_sec))
            }
        } else {
            buildString {
                append(mMinute)
                append(" ")
                append(context.getString(R.string.label_min))
            }
        }

        if (mHours != 0L) {
            mTime = buildString {
                append(mHours)
                append(" ")
                append(context.getString(R.string.label_hr))
                append(" ")
                append(mTime)
            }
        }
        return mTime
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun isGPSEnabled(mContext: Context): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isMetricUsingCountry(): Boolean {
        val locale = Locale.getDefault()
        return when (locale.country.uppercase()) {
            "US", "MM", "LR" -> false
            else -> true
        }
    }

    fun getDistanceUnit(distanceUnit: String?): String {
        return when (distanceUnit) {
            "Metric", "metric" -> KILOMETERS
            "Imperial", "imperial" -> MILES
            else -> {
                if (isMetricUsingCountry()) KILOMETERS else MILES
            }
        }
    }

    fun isMetric(distanceUnit: String?): Boolean {
        return when (distanceUnit) {
            "Metric", "metric" -> true
            "Imperial", "imperial" -> false
            else -> {
                isMetricUsingCountry()
            }
        }
    }

    fun readRouteData(context: Context): RouteSimulationData? {
        val assetManager: AssetManager = context.assets
        val inputStream: InputStream = assetManager.open("route_data.json")
        val inputStreamReader = InputStreamReader(inputStream)

        return Gson().fromJson(inputStreamReader, RouteSimulationData::class.java)
    }

    fun getDefaultIdentityPoolId(
        selectedRegion: String?,
        nearestRegion: String?
    ) = when (selectedRegion) {
        regionDisplayName[0] -> {
            when (nearestRegion) {
                regionList[0] -> {
                    BuildConfig.DEFAULT_IDENTITY_POOL_ID
                }
                regionList[1] -> {
                    BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU
                }
                regionList[2] -> {
                    BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP
                }
                else -> {
                    BuildConfig.DEFAULT_IDENTITY_POOL_ID
                }
            }
        }
        regionDisplayName[1] -> {
            BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU
        }
        regionDisplayName[2] -> {
            BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP
        }
        regionDisplayName[3] -> {
            BuildConfig.DEFAULT_IDENTITY_POOL_ID
        }
        else -> {
            BuildConfig.DEFAULT_IDENTITY_POOL_ID
        }
    }

    fun getSimulationWebSocketUrl(
        defaultIdentityPoolId: String?
    ) = when (defaultIdentityPoolId) {
        BuildConfig.DEFAULT_IDENTITY_POOL_ID -> {
            BuildConfig.SIMULATION_WEB_SOCKET_URL
        }
        BuildConfig.DEFAULT_IDENTITY_POOL_ID_EU -> {
            BuildConfig.SIMULATION_WEB_SOCKET_URL_EU
        }
        BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP -> {
            BuildConfig.SIMULATION_WEB_SOCKET_URL_AP
        }
        else -> {
            BuildConfig.SIMULATION_WEB_SOCKET_URL
        }
    }

    fun sanitizeUrl(url: String): String {
        var sanitizedUrl = url.replaceFirst("http://", "")
        sanitizedUrl = sanitizedUrl.replaceFirst("https://", "")

        if (sanitizedUrl.endsWith("/")) {
            sanitizedUrl = sanitizedUrl.substring(0, sanitizedUrl.length - 1)
        }

        return sanitizedUrl
    }

    @ExcludeFromJacocoGeneratedReport
    fun checkInternetConnection(context: Context): Boolean {
        return context.isInternetAvailable()
    }
}
