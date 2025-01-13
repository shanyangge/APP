package com.example.app.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: Activity,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {
    fun checkAndRequestPermissions(): Boolean {
        Log.d("PermissionManager", "检查权限")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.QUERY_ALL_PACKAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("PermissionManager", "请求权限")
                requestPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
                return false
            }
        }
        Log.d("PermissionManager", "权限已授予")
        return true
    }

    fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.QUERY_ALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}