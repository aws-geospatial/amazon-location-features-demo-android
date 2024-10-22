package com.aws.amazonlocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Switch
import androidx.appcompat.widget.AppCompatButton
import aws.sdk.kotlin.services.location.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

fun Context.signOutDialog(mSignOutInterface: SignOutInterface) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(this.resources.getString(R.string.logout))
    mDialog.setMessage(this.resources.getString(R.string.are_you_sure_you_want_to_sign_out))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.logout)
    ) { dialog, _ ->
        mSignOutInterface.logout(dialog, false)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.cloudFormationInfo() {
    val view = LayoutInflater.from(this)
        .inflate(R.layout.dialog_cloud_formation_info, null, false)
    val mDialog = MaterialAlertDialogBuilder(this)
    val btnOK = view.findViewById<AppCompatButton>(R.id.btn_cloudformation_ok)
    mDialog.setView(view)
    val dialog = mDialog.show()
    btnOK.setOnClickListener {
        dialog.dismiss()
    }
}

fun Context.enableTracker(clickListener: EnableTrackerInterface) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.EnableTrackerAlertStyle)
    mDialog.setTitle(this.resources.getString(R.string.label_enable_tracker))
    mDialog.setMessage(this.resources.getString(R.string.label_enable_tracker_description))
    mDialog.setCancelable(true)
    mDialog.setPositiveButton(
        this.resources.getString(R.string.label_enable_tracker_view_terms)
    ) { dialog, _ ->
        dialog.dismiss()
        clickListener.viewTermsAndCondition(dialog)
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.label_enable_tracker_continue_to_tracker)
    ) { dialog, _ ->
        clickListener.continueToTracker(dialog)
    }
    mDialog.setOnCancelListener {
        clickListener.cancel()
    }
    mDialog.show()
}

fun Context.geofenceDeleteDialog(
    position: Int,
    data: ListGeofenceResponseEntry,
    mGeofenceDeleteInterface: GeofenceDeleteInterface
) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setMessage(resources.getString(R.string.are_you_sure_want_to_delete_geofence))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        mGeofenceDeleteInterface.deleteGeofence(position, data, dialog)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.deleteTrackingDataDialog(
    deleteTrackingDataInterface: DeleteTrackingDataInterface
) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(resources.getString(R.string.label_delete_tracking_data))
    mDialog.setMessage(resources.getString(R.string.label_are_you_sure_delete_data))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        deleteTrackingDataInterface.deleteData(dialog)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.disconnectFromAWSDialog(
    mDisconnectAWSInterface: DisconnectAWSInterface,
    isLogoutRequired: Boolean
) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setMessage(getString(R.string.are_you_sure_you_want_to_disconnect_aws))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.disconnect_aws)
    ) { dialog, _ ->
        if (isLogoutRequired) {
            mDisconnectAWSInterface.logoutAndDisconnectAWS(dialog)
        } else {
            mDisconnectAWSInterface.disconnectAWS(dialog)
        }
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.messageDialog(
    title: String,
    subTitle: String,
    isCancelable: Boolean,
    mSignOutInterface: MessageInterface
) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(title)
    mDialog.setMessage(subTitle)
    mDialog.setCancelable(isCancelable)
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        mSignOutInterface.onMessageClick(dialog)
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.locationPermissionDialog() {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(this.resources.getString(R.string.label_location_permission_denied))
    mDialog.setMessage(this.resources.getString(R.string.label_required_permission))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.userSignOutDialog() {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(this.resources.getString(R.string.label_warning))
    mDialog.setMessage(this.resources.getString(R.string.label_session_expired))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.restartAppMapStyleDialog(
    restartInterface: MapStyleRestartInterface
) {
    val layoutInflater = LayoutInflater.from(applicationContext)
    val customView = layoutInflater.inflate(R.layout.dialog_custom_layout, null)

    val switchToggle: Switch = customView.findViewById(R.id.switchToggle)

    val mDialog = MaterialAlertDialogBuilder(this, R.style.MyGrabDialogTheme)

    mDialog.setTitle(if (isRunningTest) resources.getString(R.string.grab) else resources.getString(R.string.label_restart_app_title))
    mDialog.setMessage(R.string.label_restart_app_description)
    mDialog.setView(customView)
    mDialog.setPositiveButton(
        this.resources.getString(R.string.enable_grab)
    ) { dialog, _ ->
        restartInterface.onOkClick(dialog, switchToggle.isChecked)
        dialog.dismiss()
    }
    mDialog.setNeutralButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.learn_more)
    ) { dialog, _ ->
        // dialog.dismiss()
        restartInterface.onLearnMoreClick(dialog)
    }
    mDialog.show()
}

@SuppressLint("UseSwitchCompatOrMaterialCode")
fun Context.enableOpenData(
    restartInterface: MapStyleRestartInterface
) {
    val layoutInflater = LayoutInflater.from(applicationContext)
    val customView = layoutInflater.inflate(R.layout.dialog_custom_layout, null)

    val switchToggle: Switch = customView.findViewById(R.id.switchToggle)

    val mDialog = MaterialAlertDialogBuilder(this, R.style.MyGrabDialogTheme)

    mDialog.setTitle(resources.getString(R.string.label_enable_open_data))
    mDialog.setMessage(R.string.label_enable_open_data_des)
    mDialog.setView(customView)
    mDialog.setPositiveButton(
        this.resources.getString(R.string.label_enable_open_data)
    ) { dialog, _ ->
        restartInterface.onOkClick(dialog, switchToggle.isChecked)
        dialog.dismiss()
    }
    mDialog.setNeutralButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.simulationExit(simulationInterface: SimulationDialogInterface) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setMessage(this.resources.getString(R.string.simulation_exit_title))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.exit)
    ) { dialog, _ ->
        simulationInterface.onExitClick(dialog)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.notificationPermission(notificationDialogInterface: NotificationDialogInterface) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setMessage(this.resources.getString(R.string.simulation_notification_permission))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.ok)
    ) { dialog, _ ->
        notificationDialogInterface.onOkClick(dialog)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        notificationDialogInterface.onCancelClick(dialog)
        dialog.dismiss()
    }
    mDialog.show()
}

fun Context.changeDataProviderDialog(
    changeDataProviderInterface: ChangeDataProviderInterface
) {
    val mDialog = MaterialAlertDialogBuilder(this, R.style.LogOutStyle)
    mDialog.setTitle(resources.getString(R.string.label_change_data_provider))
    mDialog.setMessage(resources.getString(R.string.label_change_data_provider_des))
    mDialog.setPositiveButton(
        this.resources.getString(R.string.label_switch_to_esri)
    ) { dialog, _ ->
        changeDataProviderInterface.changeDataProvider(dialog)
        dialog.dismiss()
    }
    mDialog.setNegativeButton(
        this.resources.getString(R.string.cancel)
    ) { dialog, _ ->
        dialog.dismiss()
    }
    mDialog.show()
}

interface EnableTrackerInterface {
    fun continueToTracker(dialog: DialogInterface)
    fun cancel()
    fun viewTermsAndCondition(dialog: DialogInterface)
}

interface DisconnectAWSInterface {
    fun disconnectAWS(dialog: DialogInterface)
    fun logoutAndDisconnectAWS(dialog: DialogInterface)
}

interface SignOutInterface {
    fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean)
}

interface GeofenceDeleteInterface {
    fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry, dialog: DialogInterface)
}

interface ChangeDataProviderInterface {
    fun changeDataProvider(dialog: DialogInterface)
}

interface DeleteTrackingDataInterface {
    fun deleteData(dialog: DialogInterface)
}

interface MessageInterface {
    fun onMessageClick(dialog: DialogInterface)
}

interface MapStyleRestartInterface {
    fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean)
    fun onLearnMoreClick(dialog: DialogInterface)
}

interface SimulationDialogInterface {
    fun onExitClick(dialog: DialogInterface)
}

interface NotificationDialogInterface {
    fun onOkClick(dialog: DialogInterface)
    fun onCancelClick(dialog: DialogInterface)
}
