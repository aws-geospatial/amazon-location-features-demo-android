package com.aws.amazonlocation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aws.amazonlocation.ui.main.CrashListener
import dagger.hilt.android.HiltAndroidApp
import kotlin.system.exitProcess

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltAndroidApp
class AmazonLocationApp : Application() {

    private var crashListener: CrashListener? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setupUncaughtExceptionHandler()
    }
    private fun setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            notifyAppCrash(throwable.message ?: throwable.toString())
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
    fun setCrashListener(listener: CrashListener) {
        crashListener = listener
    }

    private fun notifyAppCrash(message: String?) {
        crashListener?.notifyAppCrash(message)
    }
}
