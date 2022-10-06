package com.lorenzofelletti.simpleblescanner.blescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.lorenzofelletti.simpleblescanner.BuildConfig

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

    var toExecuteBeforeScan: MutableList<() -> Unit> = mutableListOf()
    var toExecuteAfterScan: MutableList<() -> Unit> = mutableListOf()

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
        BleScanManagerUtilities.executeListOfFunctions(toExecuteBeforeScan)
    }

    private fun executeAfterScan() {
        BleScanManagerUtilities.executeListOfFunctions(toExecuteAfterScan)
    }

    /**
     * @return a Map mapping each permission required to use BLE to a request code
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun getBleRequiredPermissionsAndRequestCodesMap(): Map<String, Int> {
        return BleScanManagerUtilities.bleRequiredPermissionsAndRequestCodesMap
    }

    companion object {
        private var TAG = BleScanManager::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG

        /**
         * Constant holding the default max scan period time, i.e. the max number of millis
         * scanning will be performed.
         */
        const val DEFAULT_SCAN_PERIOD: Long = 10000
    }
}