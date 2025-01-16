package com.example.app

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.ReminderType
import com.example.app.ui.screens.AppListScreen
import com.example.app.ui.screens.SettingsScreen
import com.example.app.ui.screens.StatisticsScreen
import com.example.app.ui.viewmodels.AppSettingsViewModel
import com.example.app.utils.PermissionManager
import com.example.app.utils.FilePickerHelper
import com.example.app.service.UsageMonitorService
import android.app.ActivityManager

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    private lateinit var viewModel: AppSettingsViewModel

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.handleSelectedFile(uri, ReminderType.IMAGE)
            }
        }
    }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.handleSelectedFile(uri, ReminderType.AUDIO)
            }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.handleSelectedFile(uri, ReminderType.VIDEO)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "权限请求结果: $isGranted")
        if (isGranted) {
            setupUI()
        } else {
            showPermissionExplanationDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AppSettingsViewModel()
        permissionManager = PermissionManager(this, requestPermissionLauncher)

        if (permissionManager.checkAndRequestPermissions()) {
            setupUI()
        }

        if (checkUsageStatsPermission()) {
            startService(Intent(this, UsageMonitorService::class.java))
        } else {
            requestUsageStatsPermission()
        }

        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // 请求悬浮窗权限
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun testUsageStats() {
        if (!checkUsageStatsPermission()) {
            Log.d("UsageTest", "没有使用情况访问权限")
            requestUsageStatsPermission()
            return
        }

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(currentTime - 5 * 60 * 1000, currentTime)
        val event = UsageEvents.Event()
        
        Log.d("UsageTest", "开始查询使用情况...")
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    Log.d("UsageTest", "应用启动: ${event.packageName}")
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    Log.d("UsageTest", "应用退出: ${event.packageName}")
                }
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("此应用需要相关权限才能正常运行")
            .setPositiveButton("确定") { _, _ ->
                permissionManager.checkAndRequestPermissions()
            }
            .setNegativeButton("取消") { _, _ ->
                finish()
            }
            .show()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setupUI() {
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.AppList) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.List, "应用列表") },
                                label = { Text("应用") },
                                selected = currentScreen is Screen.AppList,
                                onClick = { currentScreen = Screen.AppList }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, "设置") },
                                label = { Text("设置") },
                                selected = currentScreen is Screen.Settings,
                                onClick = { currentScreen = Screen.Settings }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.InsertChart, "统计") },
                                label = { Text("统计") },
                                selected = currentScreen is Screen.Statistics,
                                onClick = { currentScreen = Screen.Statistics }
                            )
                        }
                    }
                ) { paddingValues ->
                    when (currentScreen) {
                        is Screen.AppList -> AppListScreen(
                            modifier = Modifier.padding(paddingValues),
                            onPickFile = { reminderType ->
                                when (reminderType) {
                                    ReminderType.IMAGE -> {
                                        imagePickerLauncher.launch(
                                            FilePickerHelper.createImagePickerIntent()
                                        )
                                    }
                                    ReminderType.AUDIO -> {
                                        audioPickerLauncher.launch(
                                            FilePickerHelper.createAudioPickerIntent()
                                        )
                                    }
                                    ReminderType.VIDEO -> {
                                        videoPickerLauncher.launch(
                                            FilePickerHelper.createVideoPickerIntent()
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        )
                        is Screen.Settings -> SettingsScreen(
                            modifier = Modifier.padding(paddingValues)
                        )
                        is Screen.Statistics -> StatisticsScreen(
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    override fun onResume() {
        super.onResume()
        // 检查服务是否在运行，如果没有则重启
        if (checkUsageStatsPermission() && !isServiceRunning(UsageMonitorService::class.java)) {
            startService(Intent(this, UsageMonitorService::class.java))
        }
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        try {
            // 对于 Android 11 (API 30) 及以上版本，直接返回 true
            // 因为新版本 Android 不再支持查询其他应用的服务状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return true
            }
            
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return manager.getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == serviceClass.name }
        } catch (e: Exception) {
            Log.e("MainActivity", "检查服务状态失败", e)
            return false
        }
    }
}

sealed class Screen {
    object AppList : Screen()
    object Settings : Screen()
    object Statistics : Screen()
}