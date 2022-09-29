package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var btnStartScan: Button
    private val bleScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "bleScanCallback - onScanResults called")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "bleScanCallback - onBatchScanResults called")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "bleScanCallback - scan failed with error '$errorCode'")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter
        bleScanner = btAdapter.bluetoothLeScanner

        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "btnStartScan - onClick event")

            // checks bluetooth permission
            checkPermission(Manifest.permission.BLUETOOTH_SCAN, BLUETOOTH_SCAN_REQUEST_CODE)
            // checks access coarse location permission. This permission is mandatory to get scan results
            checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                ACCESS_COARSE_LOCATION_REQUEST_CODE
            )
            // checks access fine location permission
            checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                ACCESS_FINE_LOCATION_REQUEST_CODE
            )


            // starts BLE scanning if permission was already granted˙
            startBleDeviceScanning()
        }
    }


    /**
     * Checks for a permission
     */
    private fun checkPermission(permission: String, requestCode: Int) {
        if (DEBUG) Log.d(TAG, "checkPermission - called")

        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (DEBUG) {
                Log.w(TAG, "checkPermission - '$permission' not granted")
                Log.d(TAG, "checkPermission - asking for '$permission' permission")
            }
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else if (DEBUG) {
            Log.d(TAG, "checkPermission - $permission already granted")
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BLUETOOTH_SCAN_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    if (DEBUG) {
                        permissions.forEach {
                            Log.d(TAG, "onRequestPermissionsResult - $it granted")
                        }
                    }
                    // starts the scanning if permission is granted
                    startBleDeviceScanning()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult - $permissions not granted")
                    // TODO: handling what to do if permission is not granted
                }
            }
        }
        return
    }

    /**
     * Starts the BLE devices scanning
     */
    private fun startBleDeviceScanning() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "startBleDeviceScanning - permissions granted, scanning can start")
            scanBleDevice()
        } else {
            Log.d(TAG, "startBleDeviceScanning - permissions not granted, cannot start scan")
        }
    }

    /**
     * Scans for Bluetooth LE devices and stops the scan after [SCAN_PERIOD] seconds.
     * Does not checks the required permissions are granted, check must be done beforehand.
     */
    @SuppressLint("MissingPermission")
    private fun scanBleDevice() {
        // scans for bluetooth LE devices
        if (scanning) {
            if (DEBUG) Log.d(TAG, "scanBleDevice - scan stop")
            scanning = false
            bleScanner.stopScan(bleScanCallback)
        } else {
            // stops scanning after SCAN_PERIOD millis
            handler.postDelayed({
                scanning = false
                bleScanner.stopScan(bleScanCallback)
            }, SCAN_PERIOD)
            // starts scanning
            if (DEBUG) Log.d(TAG, "scanBleDevice - scan start")
            scanning = true
            bleScanner.startScan(bleScanCallback)
        }
    }

    companion object {
        private var TAG = Companion::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG
        private const val BLUETOOTH_SCAN_REQUEST_CODE = 100
        private const val ACCESS_COARSE_LOCATION_REQUEST_CODE = 101
        private const val ACCESS_FINE_LOCATION_REQUEST_CODE = 102

        /**
         * Constant holding the scan period time, i.e. the max number of millis
         * scanning will be performed.
         */
        const val SCAN_PERIOD: Long = 10000
    }
}