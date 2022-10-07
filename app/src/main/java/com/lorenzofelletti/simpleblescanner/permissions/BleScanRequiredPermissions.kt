package com.lorenzofelletti.simpleblescanner.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

object BleScanRequiredPermissions {
    private const val BLUETOOTH_SCAN_REQUEST_CODE = 100
    private const val ACCESS_COARSE_LOCATION_REQUEST_CODE = 101
    private const val ACCESS_FINE_LOCATION_REQUEST_CODE = 102
    private const val BLUETOOTH_ADMIN_REQUEST_CODE = 103

    /** Maps each permission required for using BLE to a request code. */
    @RequiresApi(Build.VERSION_CODES.S)
    val permissionsMap = mapOf(
        Manifest.permission.BLUETOOTH_SCAN to BLUETOOTH_SCAN_REQUEST_CODE,
        Manifest.permission.ACCESS_COARSE_LOCATION to ACCESS_COARSE_LOCATION_REQUEST_CODE,
        Manifest.permission.ACCESS_FINE_LOCATION to ACCESS_FINE_LOCATION_REQUEST_CODE,
        Manifest.permission.BLUETOOTH_ADMIN to BLUETOOTH_ADMIN_REQUEST_CODE,
    )
}