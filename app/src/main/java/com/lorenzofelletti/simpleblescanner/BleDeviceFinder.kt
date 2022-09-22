package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat

class BleDeviceFinder(private val context: Context) {
    private val DEBUG: Boolean = BuildConfig.DEBUG
    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val btAdapter = btManager.adapter
    private val bleScanner = btAdapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val bleScanCallback = BleScanCallback()

    /**
     * Scans for Bluetooth LE devices and stops the scan after [SCAN_PERIOD] seconds
     */
    private fun scanBleDevice() {
        // checks bluetooth permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED) {
            if (DEBUG) Log.w(TAG, "scanBleDevice - bluetooth permission not granted")
            return
        }
        if (scanning) {
            if (DEBUG) Log.d(TAG, "scanBleDevice - scan stop")
            scanning = false
            bleScanner.stopScan(bleScanCallback)
        } else {
            // stops scanning after SCAN_PERIOD millis
            handler.postDelayed({
                scanning = false
                bleScanner.stopScan(bleScanCallback)
            }, Companion.SCAN_PERIOD)
            // starts scanning
            if (DEBUG) Log.d(TAG, "scanBleDevice - scan start")
            scanning = true
            bleScanner.startScan(bleScanCallback)
        }
    }

    companion object {
        private const val TAG = "BleDeviceFinder"

        /**
         * Constant holding the scan period time, i.e. the max number of millis
         * scanning will be performed.
         */
        const val SCAN_PERIOD: Long = 10000
    }
}