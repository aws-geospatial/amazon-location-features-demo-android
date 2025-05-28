package com.aws.amazonlocation.ui.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import aws.sdk.kotlin.services.cognitoidentity.model.ExternalServiceException
import aws.sdk.kotlin.services.cognitoidentity.model.NotAuthorizedException
import aws.sdk.kotlin.services.cognitoidentity.model.ResourceConflictException
import aws.sdk.kotlin.services.cognitoidentity.model.ResourceNotFoundException
import aws.sdk.kotlin.services.location.model.LocationException
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.simulation.SimulationUtils
import com.aws.amazonlocation.ui.main.tracking.TrackingUtils
import com.aws.amazonlocation.utils.BottomSheetHelper
import com.aws.amazonlocation.utils.KEY_LOCATION_PERMISSION
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.LatencyChecker
import com.aws.amazonlocation.utils.LocaleHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.regionList
import com.aws.amazonlocation.utils.restartApplication
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    private var mAlertDialog: AlertDialog? = null

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    var mTrackingUtils: TrackingUtils? = null
    var mSimulationUtils: SimulationUtils? = null
    var isTablet: Boolean = false

    @Inject
    lateinit var mBottomSheetHelper: BottomSheetHelper

    @Inject
    lateinit var mLocationProvider: LocationProvider

    private var subTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            if (mPreferenceManager.getValue(KEY_NEAREST_REGION, "") == "") {
                if (Units.checkInternetConnection(applicationContext)) {
                    val latencyChecker = LatencyChecker()
                    val urls = arrayListOf<String>()
                    regionList.forEach {
                        urls.add(String.format(BuildConfig.AWS_NEAREST_REGION_CHECK_URL, it))
                    }

                    val (fastestUrl, _) = runBlocking { latencyChecker.checkLatencyForUrls(urls) }
                    regionList.forEach {
                        if (fastestUrl != null) {
                            if (fastestUrl.contains(it)) {
                                mPreferenceManager.setValue(KEY_NEAREST_REGION, it)
                            }
                        }
                    }
                } else {
                    mPreferenceManager.setValue(KEY_NEAREST_REGION, regionList[0])
                }
            }

            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val preference = PreferenceManager(applicationContext)
            mTrackingUtils = TrackingUtils(preference, this@BaseActivity, mLocationProvider)
            mSimulationUtils = SimulationUtils(preference, this@BaseActivity, mLocationProvider)
            locationPermissionDialog()
        }
    }

    fun showError(error: String) {
        val snackBar =
            Snackbar.make(
                findViewById(R.id.nav_host_fragment),
                error,
                Snackbar.LENGTH_SHORT
            )
        val textView =
            snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 10
        snackBar.show()
    }

    suspend fun initMobileClient() {
        mLocationProvider.initializeLocationCredentialsProvider(this)
        mLocationProvider.initPlaceRoutesClients(this)
    }

    private fun locationPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        dialogBuilder
            .setMessage(resources.getString(R.string.location_permission_is_required))
            ?.setCancelable(true)
            ?.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                startActivity(
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
            }
        mAlertDialog = dialogBuilder.create()
        mAlertDialog?.setTitle(resources.getString(R.string.permission))
    }

    fun showLocationPermissionDialogBox() {
        mAlertDialog?.isShowing?.let {
            if (!it) {
                mAlertDialog?.show()
            }
        }
    }

    fun getLocationPermissionCount(): Int = mPreferenceManager.getIntValue(
        KEY_LOCATION_PERMISSION,
        0
    )

    fun updateLocationPermission(isLocation: Boolean = false) {
        if (getLocationPermissionCount() <= 1) {
            mPreferenceManager.setValue(
                KEY_LOCATION_PERMISSION,
                if (isLocation) 2 else getLocationPermissionCount().plus(1)
            )
        }
    }

    fun resetLocationPermission() {
        mPreferenceManager.setValue(KEY_LOCATION_PERMISSION, 0)
    }

    fun bottomNavigationVisibility(isVisibility: Boolean) {
        if (this is MainActivity) {
            this.manageBottomNavigationVisibility(isVisibility)
        }
    }

    fun handleException(
        exception: Exception,
        message: String? = null
    ) {
        subTitle = ""
        when (exception) {
            is LocationException -> {
                if (exception.message.contains("expired") || subTitle.contains("invalid")) {
                    subTitle = "Stack is expired, refreshing"
                }
            }
            is NotAuthorizedException -> {
                setTitle()
            }
            is ResourceNotFoundException -> {
                setTitle()
            }
            is ResourceConflictException -> {
                setTitle()
            }
            is ExternalServiceException -> {
                setTitle()
            }
        }
        runOnUiThread {
            if (subTitle.isEmpty()) {
                if (message != null) {
                    if (message.isNotEmpty()) {
                        showError(message)
                    }
                } else {
                    exception.message?.let {
                        showError(it)
                    }
                }
            } else if (subTitle.contains("expired") || subTitle.contains("invalid")) {
                mLocationProvider.checkSessionValid(this)
            } else {
                showErrorDialog(subTitle)
            }
        }
    }

    private fun setTitle() {
        subTitle = getString(R.string.reconnect_with_aws_account)
    }

    private fun showErrorDialog(subTitle: String) {
        runOnUiThread {
            showError(subTitle)
            restartAppWithClearData()
        }
    }

    fun restartAppWithClearData() {
        lifecycleScope.launch {
            mLocationProvider.clearCredentials()
            delay(RESTART_DELAY)
            restartApplication()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val updatedContext = LocaleHelper.attachBaseContext(newBase)
        super.attachBaseContext(updatedContext)
    }
}
