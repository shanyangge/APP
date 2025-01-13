package com.example.app

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.ReminderType
import com.example.app.ui.screens.AppListScreen
import com.example.app.ui.screens.SettingsScreen
import com.example.app.ui.screens.StatisticsScreen
import com.example.app.ui.viewmodels.AppSettingsViewModel
import com.example.app.utils.PermissionManager
import com.example.app.utils.FilePickerHelper
import com.example.app.service.UsageMonitorService

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

        // 启动监控服务
        if (checkUsageStatsPermission()) {
            startService(Intent(this, UsageMonitorService::class.java))
        } else {
            requestUsageStatsPermission()
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
        return try {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            ) == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e("MainActivity", "检查使用情况权限时出错", e)
            false
        }
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}

sealed class Screen {
    object AppList : Screen()
    object Settings : Screen()
    object Statistics : Screen()
}