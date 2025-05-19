package com.aws.amazonlocation.utils

import android.content.Context
import android.content.DialogInterface
import com.aws.amazonlocation.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

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

interface SimulationDialogInterface {
    fun onExitClick(dialog: DialogInterface)
}

interface NotificationDialogInterface {
    fun onOkClick(dialog: DialogInterface)
    fun onCancelClick(dialog: DialogInterface)
}
