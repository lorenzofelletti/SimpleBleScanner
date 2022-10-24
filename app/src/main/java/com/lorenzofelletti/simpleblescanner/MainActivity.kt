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
import com.lorenzofelletti.simpleblescanner.permissions.PermissionsUtilities.dispatchOnRequestPermissionsResult

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager

    private lateinit var foundDevices: MutableList<BleDevice>

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
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

        // Adding the actions the manager must do before and after scanning
        bleScanManager.beforeScanActions.add { btnStartScan.isEnabled = false }
        bleScanManager.beforeScanActions.add {
            foundDevices.clear()
            adapter.notifyDataSetChanged()
        }
        bleScanManager.afterScanActions.add { btnStartScan.isEnabled = true }

        // Adding the onclick listener to the start scan button
        btnStartScan = findViewById(R.id.btn_start_scan)
        btnStartScan.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")

            // Checks if the required permissions are granted and starts the scan if so, otherwise it requests them
            when (PermissionsUtilities.checkPermissionsGranted(
                this,
                BleScanRequiredPermissions.permissions
            )) {
                true -> bleScanManager.scanBleDevices()
                false -> PermissionsUtilities.checkPermissions(
                    this, BleScanRequiredPermissions.permissions, BLE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        dispatchOnRequestPermissionsResult(
            requestCode,
            grantResults,
            onGrantedMap = mapOf(BLE_PERMISSION_REQUEST_CODE to {
                bleScanManager.scanBleDevices()
            }),
            onDeniedMap = mapOf(BLE_PERMISSION_REQUEST_CODE to {
                Toast.makeText(
                    this,
                    "Some permissions were not granted, please grant them and try again",
                    Toast.LENGTH_LONG
                ).show()
            })
        )
    }

    companion object {
        private var TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG
        private const val BLE_PERMISSION_REQUEST_CODE = 1
    }
}