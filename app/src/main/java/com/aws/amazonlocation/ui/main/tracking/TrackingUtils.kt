package com.aws.amazonlocation.ui.main.tracking

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetStartSimulationBinding
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.NotificationDialogInterface
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.notificationPermission
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.simulationFields
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class TrackingUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mLocationProvider: LocationProvider
) {
    var isChangeDataProviderClicked: Boolean = false
    private var mBottomSheetTrackingBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private lateinit var mBinding: BottomSheetStartSimulationBinding
    private var mFragmentActivity: FragmentActivity? = null

    fun isTrackingExpandedOrHalfExpand(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isTrackingSheetCollapsed(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isTrackingSheetHidden(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HIDDEN || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_SETTLING
    }

    fun collapseTracking() {
        mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mBottomSheetTrackingBehavior?.isDraggable = true
    }

    fun showTrackingBottomSheet() {
        clickListener()
        mBottomSheetTrackingBehavior?.isHideable = true
        mBottomSheetTrackingBehavior?.isDraggable = false
        mBottomSheetTrackingBehavior?.isFitToContents = false
        mBinding.clTrackerGeofence.context?.let {
            if ((activity as MainActivity).isTablet) {
                mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_150
                )
            } else {
                mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_110
                )
            }
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun initTrackingView(
        fragmentActivity: FragmentActivity?,
        bottomSheetGeofenceList: BottomSheetStartSimulationBinding
    ) {
        this.mFragmentActivity = fragmentActivity
        this.mBinding = bottomSheetGeofenceList
        initTrackingBottomSheet()
    }

    private fun initTrackingBottomSheet() {
        mBinding.apply {
            mBottomSheetTrackingBehavior = BottomSheetBehavior.from(root)
            mBottomSheetTrackingBehavior?.isHideable = true
            mBottomSheetTrackingBehavior?.isDraggable = false
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetTrackingBehavior?.isFitToContents = false
            mBottomSheetTrackingBehavior?.halfExpandedRatio = 0.58f
        }
    }

    fun hideTrackingBottomSheet() {
        mBottomSheetTrackingBehavior.let {
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
        }
    }

    private fun clickListener() {
        mBinding.apply {
            btnStartSimulation.setOnClickListener {
                val simulationMissingFields = simulationFields.filter { it.value == "null" }.keys

                if (simulationMissingFields.isNotEmpty()) {
                    activity?.apply {
                        val dialogMessage =
                            buildString {
                                append(getString(R.string.label_simulation_fields_missing))
                                append("\n")
                                simulationMissingFields.forEach { append("• $it\n") }
                            }

                        val dialogTitle: String = getString(R.string.title_configuration_incomplete)
                        val positiveButtonText: String = getString(R.string.ok)

                        AlertDialog
                            .Builder(activity)
                            .setTitle(dialogTitle)
                            .setMessage(dialogMessage)
                            .setPositiveButton(positiveButtonText) { dialog, _ ->
                                dialog.dismiss()
                            }.setCancelable(false).show()
                    }
                } else {
                    startSimulation()
                }
            }
        }
    }

    private fun startSimulation() {
        when {
            ContextCompat.checkSelfPermission(
                (activity as MainActivity).applicationContext,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                openSimulation()
            }

            activity.shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) -> {
                if (!NotificationManagerCompat.from(activity.applicationContext).areNotificationsEnabled()) {
                    activity.applicationContext.notificationPermission(object : NotificationDialogInterface {
                        override fun onOkClick(dialog: DialogInterface) {
                            openAppNotificationSettings(activity.applicationContext)
                        }

                        override fun onCancelClick(dialog: DialogInterface) {
                            openSimulation()
                        }
                    })
                }
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(
                        POST_NOTIFICATIONS
                    )
                } else {
                    openSimulation()
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun openAppNotificationSettings(context: Context) {
        val intent = Intent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // For Android 8.0 (Oreo) and above, open the notification settings for the app
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                // For Android 5.0 (Lollipop) and above, open the app's notification settings
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                }
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
            else -> {
                // For older Android versions, open the app details settings
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
    }
    private val requestPermissionLauncher = (activity as MainActivity).registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        openSimulation()
    }

    private fun openSimulation() {
        val properties = listOf(
            Pair(AnalyticsAttribute.SCREEN_NAME, AnalyticsAttributeValue.SIMULATION)
        )
        (activity as MainActivity).analyticsUtils?.recordEvent(
            EventType.START_SIMULATION,
            properties
        )
        activity.showSimulationSheet()
    }
}
