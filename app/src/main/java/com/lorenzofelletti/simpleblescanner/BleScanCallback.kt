package com.lorenzofelletti.simpleblescanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class BleScanCallback: ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        if (DEBUG) Log.i(TAG, "onScanResult - called")
        super.onScanResult(callbackType, result)
    }

    companion object {
        private val DEBUG: Boolean = BuildConfig.DEBUG
        private const val TAG = "BleScanCallback"
    }
}