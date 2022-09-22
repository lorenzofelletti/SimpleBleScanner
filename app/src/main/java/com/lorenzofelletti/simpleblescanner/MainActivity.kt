package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button
    private lateinit var bleDeviceFinder: BleDeviceFinder

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // baseContext offers the activity context
        bleDeviceFinder = BleDeviceFinder(this.baseContext)

        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "btnStartScan - onClick event")

            // checks bluetooth permission
            checkPermission(Manifest.permission.BLUETOOTH_SCAN, BLUETOOTH_SCAN_REQUEST_CODE)

            // starts BLE scanning if permission was already granted
            startBleDeviceScanning()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BLUETOOTH_SCAN_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (DEBUG) {
                        permissions.forEach {
                            Log.d(TAG, "onRequestPermissionsResult - $it granted")
                        }
                    }
                    // starts the scanning if permission is granted
                    startBleDeviceScanning()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult - $permissions not granted")
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
        ) == PackageManager.PERMISSION_GRANTED)
            bleDeviceFinder.scanBleDevice()
    }

    /**
     * Checks for a permission
     */
    private fun checkPermission(permission: String, requestCode: Int) {
        if (DEBUG) Log.d(TAG, "checkPermission - called")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_DENIED) {
            if (DEBUG) {
                Log.w(TAG, "checkPermission - '$permission' not granted")
                Log.d(TAG, "checkPermission - asking for '$permission' permission")
            }
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else if (DEBUG) {
            Log.d(TAG, "checkPermission - $permission already granted")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private val DEBUG: Boolean = BuildConfig.DEBUG
        private const val BLUETOOTH_SCAN_REQUEST_CODE = 100
    }
}