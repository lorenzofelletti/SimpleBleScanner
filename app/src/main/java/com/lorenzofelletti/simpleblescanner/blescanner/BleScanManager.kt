package com.lorenzofelletti.simpleblescanner.blescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.lorenzofelletti.simpleblescanner.BuildConfig
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleScanCallback

/**
 * A manager for bluetooth LE scanning..
 */
class BleScanManager(
    btManager: BluetoothManager,
    private val scanPeriod: Long = DEFAULT_SCAN_PERIOD,
    private val scanCallback: BleScanCallback = BleScanCallback()
) {
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner

    var beforeScanActions: MutableList<() -> Unit> = mutableListOf()
    var afterScanActions: MutableList<() -> Unit> = mutableListOf()

    /** True when the manager is performing the scan */
    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Scans for Bluetooth LE devices and stops the scan after [scanPeriod] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevice() {
        fun stopScan() {
            if (DEBUG) Log.d(TAG, "${::scanBleDevice.name} - scan stop")
            scanning = false
            bleScanner.stopScan(scanCallback)

            // execute all the functions to execute after scanning
            executeAfterScan()
        }

        // scans for bluetooth LE devices
        if (scanning) {
            stopScan()
        } else {
            // stops scanning after scanPeriod millis
            handler.postDelayed({ stopScan() }, scanPeriod)
            // execute all the functions to execute before scanning
            executeBeforeScan()

            // starts scanning
            if (DEBUG) Log.d(TAG, "${::scanBleDevice.name} - scan start")
            scanning = true
            bleScanner.startScan(scanCallback)
        }
    }

    private fun executeBeforeScan() {
        executeListOfFunctions(beforeScanActions)
    }

    private fun executeAfterScan() {
        executeListOfFunctions(afterScanActions)
    }

    companion object {
        /**
         * Constant holding the default max scan period time, i.e. the max number of millis
         * scanning will be performed.
         */
        const val DEFAULT_SCAN_PERIOD: Long = 10000
        private var TAG = BleScanManager::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG

        /** Execute a [List] of functions taking no arguments and returning [Unit]. */
        fun executeListOfFunctions(toExecute: List<() -> Unit>) {
            toExecute.forEach {
                it()
            }
        }
    }
}