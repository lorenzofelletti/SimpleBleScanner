package com.lorenzofelletti.simpleblescanner.blescanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.lorenzofelletti.simpleblescanner.BuildConfig

class BleScanCallback : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        if (DEBUG) Log.d(TAG, "bleScanCallback - onScanResults called")
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        if (DEBUG) Log.d(TAG, "bleScanCallback - onBatchScanResults called")
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        if (DEBUG) Log.e(TAG, "bleScanCallback - scan failed with error '$errorCode'")
    }

    companion object {
        private val TAG = Companion::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}