package com.lorenzofelletti.simpleblescanner.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * An object containing the required permissions for scanning bluetooth LE devices.
 */
object BleScanRequiredPermissions {

    /** Array of required permissions for BLE scanning. */
    @RequiresApi(Build.VERSION_CODES.S)
    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
    )
}