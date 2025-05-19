package com.aws.amazonlocation.utils.screenshotRule

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File

fun takeScreenshot(parentFolderPath: String = "", screenShotName: String) {
    Log.d(TAG, "Taking screenshot of '$screenShotName'")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val screenshotFile = File(parentFolderPath, "$screenShotName.png")
    val success = device.takeScreenshot(screenshotFile)

    if (success) {
        Log.d(TAG, "Screenshot taken and saved at: ${screenshotFile.absolutePath}")
    } else {
        Log.e(TAG, "Failed to take screenshot")
    }
}

private const val TAG = "Screenshots"
