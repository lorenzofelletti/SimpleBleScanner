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
     * Checks for a set of permissions. If not granted, the user is asked to grant them.
     *
     * @param activity The activity that is requesting the permissions
     * @param permissions The permissions to be checked
     * @param requestCode The request code to be used when requesting the permissions
     */
    fun checkPermissions(
        activity: Activity, permissions: Array<out String>, requestCode: Int
    ) {
        val name = object {}.javaClass.enclosingMethod?.name // get method name for debug

        val permissionsNotGranted = permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                activity.baseContext, permission
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNotGranted.isNotEmpty()) {
            if (DEBUG) Log.d(TAG, "$name - requesting permissions $permissionsNotGranted")

            ActivityCompat.requestPermissions(activity, permissionsNotGranted, requestCode)
        } else {
            if (DEBUG) Log.d(TAG, "$name - permissions $permissions already granted")
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     *
     * @param permissions The permissions that were requested
     * @param grantResults The results of the permission request
     *
     * @return true if all permissions were granted, false otherwise
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

    /**
     * Checks whether a set of permissions is granted or not
     *
     * @param context The context to be used for checking the permissions
     * @param permissions The permissions to be checked
     *
     * @return true if all permissions are granted, false otherwise
     */
    fun checkPermissionsGranted(context: Context, permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (!checkPermissionGranted(context, permission)) return false
        }
        return true
    }

    /**
     * Checks whether a permission is granted in the context
     *
     * @param context The context to be used for checking the permission
     * @param permission The permission to be checked
     *
     * @return true if the permission is granted, false otherwise
     */
    private fun checkPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}