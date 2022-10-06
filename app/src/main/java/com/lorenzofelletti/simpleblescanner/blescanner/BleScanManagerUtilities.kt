package com.lorenzofelletti.simpleblescanner.blescanner

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

class BleScanManagerUtilities {

    companion object {
        private val TAG = BleScanManagerUtilities::class.java.simpleName

        private const val BLUETOOTH_SCAN_REQUEST_CODE = 100
        private const val ACCESS_COARSE_LOCATION_REQUEST_CODE = 101
        private const val ACCESS_FINE_LOCATION_REQUEST_CODE = 102

        /** Maps each permission required for using BLE to a request code. */
        @RequiresApi(Build.VERSION_CODES.S)
        val bleRequiredPermissionsAndRequestCodesMap: Map<String, Int> = mapOf(
            Manifest.permission.BLUETOOTH_SCAN to BLUETOOTH_SCAN_REQUEST_CODE,
            Manifest.permission.ACCESS_COARSE_LOCATION to ACCESS_COARSE_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION to ACCESS_FINE_LOCATION_REQUEST_CODE
        )

        /** Execute a [List] of functions taking no arguments and returning [Unit]. */
        fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }
}