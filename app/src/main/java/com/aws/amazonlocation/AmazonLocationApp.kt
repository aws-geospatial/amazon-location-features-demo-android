package com.aws.amazonlocation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aws.amazonlocation.ui.main.CrashListener
import com.aws.amazonlocation.utils.AmplifyHelper
import com.aws.amazonlocation.utils.KEY_APP_CRASH
import com.aws.amazonlocation.utils.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltAndroidApp
class AmazonLocationApp : Application() {

    @Inject
    lateinit var amplifyHelper: AmplifyHelper
    @Inject
    lateinit var mPreferenceManager: PreferenceManager
    private var crashListener: CrashListener? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setupUncaughtExceptionHandler()
    }
    private fun setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            mPreferenceManager.setValue(KEY_APP_CRASH, true)
            notifyAppCrash(throwable.message)
        }
    }
    fun setCrashListener(listener: CrashListener) {
        crashListener = listener
    }

    private fun notifyAppCrash(message: String?) {
        crashListener?.notifyAppCrash(message)
    }
}
