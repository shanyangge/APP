package com.example.app.ui.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val packageManager = getApplication<Application>().packageManager
                val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                // 修改过滤逻辑，放宽条件
                val filteredApps = allApps.filter { app ->
                    val isNotSelf = app.packageName != getApplication<Application>().packageName
                    val isLaunchable = packageManager.getLaunchIntentForPackage(app.packageName) != null

                    // 移除系统应用判断，只保留可启动和非本应用的判断
                    isNotSelf && isLaunchable
                }

                val installedApps = filteredApps.map { applicationInfo ->
                    AppInfo(
                        appName = packageManager.getApplicationLabel(applicationInfo).toString(),
                        packageName = applicationInfo.packageName,
                        icon = packageManager.getApplicationIcon(applicationInfo),
                        isSelected = false
                    )
                }.sortedBy { it.appName }

                _apps.value = installedApps
            } catch (e: Exception) {
                Log.e("AppListViewModel", "加载应用列表失败", e)
            }
        }
    }

    private fun isSystemApp(flags: Int): Boolean {
        // 修改系统应用的判断逻辑
        return (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) &&
                (flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0)
    }

    fun toggleAppSelection(packageName: String) {
        val updatedApps = _apps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(isSelected = !app.isSelected)
            } else {
                app
            }
        }
        _apps.value = updatedApps
    }
}