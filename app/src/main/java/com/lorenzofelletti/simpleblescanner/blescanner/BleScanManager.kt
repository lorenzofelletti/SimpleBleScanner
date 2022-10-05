package com.lorenzofelletti.simpleblescanner.blescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.lorenzofelletti.simpleblescanner.BuildConfig

/**
 * A manager for bluetooth LE scanning..
 */
class BleScanManager() {
    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private var toExecuteBeforeScan: MutableList<() -> Unit> = mutableListOf()
    private var toExecuteAfterScan: MutableList<() -> Unit> = mutableListOf()

    fun addBeforeScanAction(action: () -> Unit) {
        toExecuteBeforeScan.add(action)
    }

    fun removeBeforeScanAction(action: () -> Unit) {
        toExecuteBeforeScan.remove(action)
    }

    fun addAfterScanAction(action: () -> Unit) {
        toExecuteAfterScan.add(action)
    }

    fun removeAfterScanAction(action: () -> Unit) {
        toExecuteAfterScan.remove(action)
    }

    /** The maximum scanning period. Default is [DEFAULT_SCAN_PERIOD]. */
    private var scanPeriod: Long = DEFAULT_SCAN_PERIOD

    /** True when the manager is performing the scan */
    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())
    private val bleScanCallback = BleScanCallback()

    constructor(btManager: BluetoothManager) : this() {
        this.btManager = btManager
        this.btAdapter = btManager.adapter
        this.bleScanner = btAdapter.bluetoothLeScanner
    }

    constructor(btManager: BluetoothManager, scanPeriod: Long) : this() {
        BleScanManager(btManager)
        this.scanPeriod = scanPeriod
    }

    /**
     * Scans for Bluetooth LE devices and stops the scan after [scanPeriod] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    fun scanBleDevice() {
        fun stopScan() {
            if (DEBUG) Log.d(TAG, "${::scanBleDevice.name} - scan stop")
            scanning = false
            bleScanner.stopScan(bleScanCallback)

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
            bleScanner.startScan(bleScanCallback)
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