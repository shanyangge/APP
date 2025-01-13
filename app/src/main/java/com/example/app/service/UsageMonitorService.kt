package com.example.app.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.app.domain.model.AppSettings
import com.example.app.utils.ReminderManager
import java.util.concurrent.TimeUnit

class UsageMonitorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val usageStatsManager by lazy {
        getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val reminderManager by lazy { ReminderManager(this) }

    private var lastCheckedTime = System.currentTimeMillis()
    private val appUsageMap = mutableMapOf<String, Long>()
    private val monitoredApps = MutableStateFlow<Map<String, AppSettings>>(emptyMap())

    override fun onCreate() {
        super.onCreate()
        startMonitoring()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                checkAppUsage()
                delay(1000) // 每秒检查一次
            }
        }
    }

    private fun checkAppUsage() {
        val currentTime = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(lastCheckedTime, currentTime)
        val usageEvent = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(usageEvent)

            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    handleAppLaunch(usageEvent.packageName)
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    updateAppUsageTime(usageEvent.packageName, usageEvent.timeStamp)
                }
            }
        }

        lastCheckedTime = currentTime
    }

    private fun handleAppLaunch(packageName: String) {
        monitoredApps.value[packageName]?.let { settings ->
            // 显示初始提醒
            reminderManager.showReminder(settings)
            // 重置使用时间计数
            appUsageMap[packageName] = System.currentTimeMillis()
        }
    }

    private fun updateAppUsageTime(packageName: String, timestamp: Long) {
        val startTime = appUsageMap[packageName] ?: return
        val usageTime = timestamp - startTime

        monitoredApps.value[packageName]?.let { settings ->
            val timeLimit = TimeUnit.MINUTES.toMillis(settings.timeLimit.toLong())
            if (usageTime >= timeLimit) {
                // 显示超时提醒
                reminderManager.showTimeoutReminder(settings)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}