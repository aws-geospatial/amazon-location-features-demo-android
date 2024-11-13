package com.aws.amazonlocation.ui.main.simulation

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetStartSimulationBinding
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.NotificationDialogInterface
import com.aws.amazonlocation.utils.notificationPermission
import com.aws.amazonlocation.utils.simulationFields
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var mBinding: BottomSheetStartSimulationBinding
    private lateinit var dialog: BottomSheetDialog

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val height = resources.getDimensionPixelSize(R.dimen.simulation_height)
        mBinding.clSimulation?.layoutParams?.height = height
        mBinding.clSimulation?.requestLayout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                behaviour.isDraggable = false
                dialog.setCancelable(false)
                behaviour.isHideable = false
                behaviour.isFitToContents = false
                if (!(activity as MainActivity).isTablet) {
                    behaviour.expandedOffset = resources.getDimension(R.dimen.dp_50).toInt()
                }
                setupFullHeight(layout)
            }
        }
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = BottomSheetStartSimulationBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            btnStartSimulation.setOnClickListener {
                val simulationMissingFields = simulationFields.filter { it.value == "null" }.keys

                if (simulationMissingFields.isNotEmpty()) {
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
                } else {
                    startSimulation()
                }
            }
            tvMaybeLater.setOnClickListener {
                dialog.dismiss()
            }
            ivStartSimulationClose.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun startSimulation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                openSimulation()
            }

            shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) -> {
                if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                    requireContext().notificationPermission(object : NotificationDialogInterface {
                        override fun onOkClick(dialog: DialogInterface) {
                            openAppNotificationSettings(requireContext())
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
    private val requestPermissionLauncher = registerForActivityResult(
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
        (activity as MainActivity).showSimulationSheet()
        dialog.dismiss()
    }

    private fun init() {
    }
}
