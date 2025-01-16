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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import com.example.app.App
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.os.Handler
import android.os.Looper

class UsageMonitorService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "usage_monitor_channel"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val usageStatsManager by lazy {
        getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val reminderManager by lazy { ReminderManager(this) }
    private val dao = App.instance.database.appSettingsDao()

    private var lastCheckedTime = System.currentTimeMillis()
    private val appUsageMap = mutableMapOf<String, Long>()
    private val monitoredApps = MutableStateFlow<Map<String, AppSettings>>(emptyMap())

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            
            serviceScope.launch {
                try {
                    dao.getAllSettings().collect { settingsList ->
                        Log.d("UsageMonitor", "更新监控应用列表: ${settingsList.size}")
                        monitoredApps.value = settingsList
                            .filter { settings -> settings.isEnabled }
                            .associateBy { settings -> settings.packageName }
                    }
                } catch (e: Exception) {
                    Log.e("UsageMonitor", "监控应用列表更新失败", e)
                }
            }
            startMonitoring()
        } catch (e: Exception) {
            Log.e("UsageMonitor", "服务启动失败", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "使用情况监控",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "监控应用使用情况"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("应用监控运行中")
            .setContentText("正在监控应用使用情况")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            try {
                while (isActive) {
                    checkAppUsage()
                    delay(1000)
                }
            } catch (e: Exception) {
                Log.e("UsageMonitor", "监控循环出错", e)
                // 尝试重启监控
                delay(1000)
                startMonitoring()
            }
        }
    }

    private fun checkAppUsage() {
        val currentTime = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(lastCheckedTime, currentTime)
        val usageEvent = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(usageEvent)
            Log.d("UsageMonitor", "事件类型: ${usageEvent.eventType}, 包名: ${usageEvent.packageName}")

            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    Log.d("UsageMonitor", "检测到应用启动事件")
                    Log.d("UsageMonitor", "当前监控的应用: ${monitoredApps.value.keys}")
                    if (monitoredApps.value.containsKey(usageEvent.packageName)) {
                        Log.d("UsageMonitor", "这是被监控的应用")
                        handleAppLaunch(usageEvent.packageName)
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    updateAppUsageTime(usageEvent.packageName, usageEvent.timeStamp)
                }
            }
        }

        lastCheckedTime = currentTime
    }

    private fun handleAppLaunch(packageName: String) {
        try {
            Log.d("UsageMonitor", "开始处理应用启动: $packageName")
            monitoredApps.value[packageName]?.let { settings ->
                Log.d("UsageMonitor", "找到应用设置: ${settings.reminderContent}")
                // 使用 Handler 在主线程显示提醒
                Handler(Looper.getMainLooper()).post {
                    try {
                        Log.d("UsageMonitor", "尝试显示提醒")
                        reminderManager.showReminder(settings)
                        Log.d("UsageMonitor", "提醒显示完成")
                    } catch (e: Exception) {
                        Log.e("UsageMonitor", "显示提醒失败", e)
                        e.printStackTrace()
                    }
                }
                appUsageMap[packageName] = System.currentTimeMillis()
            } ?: Log.d("UsageMonitor", "未找到应用设置")
        } catch (e: Exception) {
            Log.e("UsageMonitor", "处理应用启动失败", e)
            e.printStackTrace()
        }
    }

    private fun updateAppUsageTime(packageName: String, timestamp: Long) {
        val startTime = appUsageMap[packageName] ?: return
        val usageTime = timestamp - startTime

        monitoredApps.value[packageName]?.let { settings ->
            val timeLimit = TimeUnit.MINUTES.toMillis(settings.timeLimit.toLong())
            if (usageTime >= timeLimit) {
                // 使用已有的 ReminderManager 显示超时提醒
                reminderManager.showTimeoutReminder(settings)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 如果服务被杀死后重启，返回这个值
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        // 尝试重启服务
        val intent = Intent(this, UsageMonitorService::class.java)
        startService(intent)
    }
}