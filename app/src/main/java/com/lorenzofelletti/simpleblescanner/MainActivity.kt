package com.lorenzofelletti.simpleblescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanCallback
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.BleDeviceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private lateinit var bleRequiredPermissionsAndRequestCodesMap: Map<String, Int>

    private lateinit var foundDevices: MutableList<BleDevice>

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView handling
        val rvFoundDevices = findViewById<View>(R.id.rv_found_devices) as RecyclerView
        foundDevices = BleDevice.createBleDevicesList()
        val adapter = BleDeviceAdapter(foundDevices)
        rvFoundDevices.adapter = adapter
        rvFoundDevices.layoutManager = LinearLayoutManager(this)

        // BleManager creation
        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager, 5000, scanCallback = BleScanCallback({
            val name = it?.device?.address
            if (name.isNullOrBlank()) return@BleScanCallback

            val device = BleDevice(name)
            if (!foundDevices.contains(device)) {
                foundDevices.add(device)
                adapter.notifyItemInserted(foundDevices.size - 1)
            }
        }))
        bleRequiredPermissionsAndRequestCodesMap =
            bleScanManager.getBleRequiredPermissionsAndRequestCodesMap()

        // adding the actions the manager must do before and after scanning
        bleScanManager.toExecuteBeforeScan.add { btnStartScan.isEnabled = false }
        bleScanManager.toExecuteBeforeScan.add {
            foundDevices.clear()
            adapter.notifyDataSetChanged()
        }
        bleScanManager.toExecuteAfterScan.add { btnStartScan.isEnabled = true }

        // adding the onclick listener to the start scan btn
        btnStartScan = findViewById(R.id.btn_start_scan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")

            // checks that all required permissions are granted
            checkAllRequiredPermissions()

            // starts BLE scanning if permission was already granted˙
            startBleDeviceScanning()
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
                    var toastText = "Permissions "
                    permissions.forEachIndexed { idx, permission ->
                        val isLast = idx == permissions.size - 1
                        toastText += "'$permission'" + if (isLast) ", " else " "
                    }
                    toastText += "required to perform BLE scanning were not granted."
                    Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                if (DEBUG) Log.w(
                    TAG,
                    "${::onRequestPermissionsResult.name} - unexpected request code '$requestCode'"
                )
            }
        }
    }

    private fun checkAllRequiredPermissionsGranted(): Boolean {
        for (permission in bleRequiredPermissionsAndRequestCodesMap) {
            if (!checkPermissionGranted(permission.key)) return false
        }
        return true
    }

    private fun checkPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

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
        private var TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG
    }
}