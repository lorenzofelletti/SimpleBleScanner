package com.lorenzofelletti.simpleblescanner

import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager

class MainActivity : AppCompatActivity() {
    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private lateinit var bleRequiredPermissionsAndRequestCodesMap: Map<String, Int>
    private lateinit var btnStartScan: Button

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager)
        bleRequiredPermissionsAndRequestCodesMap = bleScanManager.getBleRequiredPermissionsAndRequestCodesMap()

        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName} - onClick event")

            // checks that all required permissions are granted
            checkAllRequiredPermissions()

            // starts BLE scanning if permission was already granted˙
            startBleDeviceScanning()

            // disable the button until scanning ends
            // maybe this is not the best place to put it
            btnStartScan.isEnabled = false
        }
    }

    /**
     * Checks that all the permissions required for ble are present, if not they're requested to the
     * user
     */
    private fun checkAllRequiredPermissions() {
        bleRequiredPermissionsAndRequestCodesMap.forEach {
            checkPermission(it.key, it.value)
        }
    }

    /**
     * Checks for a permission. If not present the user is asked to grant it.
     */
    private fun checkPermission(permission: String, requestCode: Int) {
        val name = object {}.javaClass.enclosingMethod?.name // get method name for debug
        if (DEBUG) Log.d(TAG, "$name - called")

        if (ActivityCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (DEBUG) {
                Log.w(TAG, "$name - '$permission' not granted")
                Log.d(TAG, "$name - asking for '$permission' permission")
            }
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else if (DEBUG) {
            Log.d(TAG, "$name - $permission already granted")
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (bleRequiredPermissionsAndRequestCodesMap.containsValue(requestCode)) {
            true -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (DEBUG) {
                        permissions.forEach {
                            Log.d(TAG, "${::onRequestPermissionsResult.name} - $it granted")
                        }
                    }
                    // starts the scanning if permission is granted
                    startBleDeviceScanning()
                } else {
                    if (DEBUG) Log.d(
                        TAG, "${::onRequestPermissionsResult.name} - $permissions not granted"
                    )
                    // TODO: handling what to do if permission is not granted
                }
            }
            else -> {
                if (DEBUG) Log.w(
                    TAG,
                    "${::onRequestPermissionsResult.name} - unexpected request code '$requestCode'"
                )
            }
        }
        return
    }

    private fun checkAllRequiredPermissionsGranted() : Boolean {
        for (permission in bleRequiredPermissionsAndRequestCodesMap) {
            if (!checkPermissionGranted(permission.key))
                return false
        }
        return true
    }

    private fun checkPermissionGranted(permission: String) : Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starts the BLE devices scanning
     */
    private fun startBleDeviceScanning() {
        if (checkAllRequiredPermissionsGranted()) {
            if (DEBUG) Log.d(
                TAG, "${::startBleDeviceScanning.name} - permissions granted, scanning can start"
            )

            // starts scan
            bleScanManager.scanBleDevice()
        } else {
            if (DEBUG) Log.d(
                TAG, "${::startBleDeviceScanning.name} - permissions not granted, cannot start scan"
            )
        }
    }

    companion object {
        private var TAG = Companion::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG
    }
}