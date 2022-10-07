package com.lorenzofelletti.simpleblescanner.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.lorenzofelletti.simpleblescanner.BuildConfig

object PermissionsUtilities {
    private var TAG = this::class.java.simpleName
    private val DEBUG = BuildConfig.DEBUG

    /**
     * Checks that all the permissions required for ble are present, if not they're requested to the
     * user
     */
    fun checkRequiredPermissions(activity: Activity, permissionsAndRequestCodes: Map<String, Int>) {
        permissionsAndRequestCodes.forEach {
            checkPermission(activity, it.key, it.value)
        }
    }

    /**
     * Checks for a permission. If not present the user is asked to grant it.
     */
    private fun checkPermission(activity: Activity, permission: String, requestCode: Int) {
        val name = object {}.javaClass.enclosingMethod?.name // get method name for debug

        if (ActivityCompat.checkSelfPermission(
                activity.baseContext, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (DEBUG) {
                Log.w(TAG, "$name - '$permission' not granted")
                Log.d(TAG, "$name - asking for '$permission' permission")
            }
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        } else if (DEBUG) {
            Log.d(TAG, "$name - $permission already granted")
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    fun checkRequestedPermissionsResults(
        permissions: Array<out String>, grantResults: IntArray
    ): Boolean {
        var res = true
        for (result in grantResults) {
            when (result) {
                PackageManager.PERMISSION_GRANTED -> {
                    if (DEBUG) {
                        permissions.forEach {
                            Log.d(TAG, "${::checkRequestedPermissionsResults.name} - $it granted")
                        }
                    }
                }
                else -> {
                    if (DEBUG) Log.d(
                        TAG, "${::checkRequestedPermissionsResults.name} - $permissions not granted"
                    )
                    res = false
                }
            }
        }
        return res
    }

    fun checkRequiredPermissionsGranted(context: Context, permissions: Set<String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionGranted(context, permission)) return false
        }
        return true
    }

    private fun checkPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}