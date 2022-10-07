package com.lorenzofelletti.simpleblescanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleScanCallback
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.BleDeviceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice
import com.lorenzofelletti.simpleblescanner.permissions.BleScanRequiredPermissions
import com.lorenzofelletti.simpleblescanner.permissions.PermissionsUtilities

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager

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
                if (DEBUG) {
                    Log.d(
                        BleScanCallback::class.java.simpleName,
                        "${this.javaClass.enclosingMethod?.name} - Found device: $name"
                    )
                }
                foundDevices.add(device)
                adapter.notifyItemInserted(foundDevices.size - 1)
            }
        }))

        // adding the actions the manager must do before and after scanning
        bleScanManager.beforeScanActions.add { btnStartScan.isEnabled = false }
        bleScanManager.beforeScanActions.add {
            foundDevices.clear()
            adapter.notifyDataSetChanged()
        }
        bleScanManager.afterScanActions.add { btnStartScan.isEnabled = true }

        // adding the onclick listener to the start scan btn
        btnStartScan = findViewById(R.id.btn_start_scan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")

            // checks that all required permissions are granted
            PermissionsUtilities.checkRequiredPermissions(
                this, BleScanRequiredPermissions.permissionsMap
            )

            // starts BLE scanning if permission was already granted˙
            startBleDeviceScanning()
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (PermissionsUtilities.checkRequestedPermissionsResults(permissions, grantResults)) {
            true -> {
                if (DEBUG) {
                    Log.d(
                        TAG,
                        "${::onRequestPermissionsResult.name} - $permissions granted!"
                    )
                }
            }
            false -> {
                // TODO: what to do if some permissions were not granted
                if (DEBUG) {
                    Log.d(
                        TAG,
                        "${::onRequestPermissionsResult.name} - some permissions in $permissions were not granted"
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startBleDeviceScanning() {
        when (PermissionsUtilities.checkRequiredPermissionsGranted(
            this.baseContext, BleScanRequiredPermissions.permissionsMap.keys
        )) {
            true -> {
                if (DEBUG) Log.d(
                    TAG,
                    "${::startBleDeviceScanning.name} - permissions granted, scanning can start"
                )

                // starts scan
                bleScanManager.scanBleDevice()
            }
            false -> {
                if (DEBUG) Log.d(
                    TAG,
                    "${::startBleDeviceScanning.name} - permissions not granted, cannot start scan"
                )
                // scanning not possible because of permissions
                Toast.makeText(
                    baseContext,
                    "Scanning not possible - required permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
                btnStartScan.isEnabled = false
            }
        }
    }

    companion object {
        private var TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG
    }
}