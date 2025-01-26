package com.example.app.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.app.R
import com.example.app.domain.model.AppSettings
import com.example.app.domain.model.ReminderType
import com.example.app.ui.reminder.ReminderActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.os.Handler
import android.os.Looper
import android.graphics.PixelFormat

class ReminderManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var hasShownReminder = false
    private var windowManager: WindowManager? = null
    private var reminderView: View? = null

    fun showReminder(appSettings: AppSettings) {
        Log.d("ReminderManager", "开始显示提醒")
        if (hasShownReminder) {
            Log.d("ReminderManager", "已经显示过提醒，跳过")
            return
        }
        
        hasShownReminder = true
        
        when (appSettings.reminderType) {
            ReminderType.TEXT -> showTextReminder(appSettings.reminderContent)
            ReminderType.IMAGE -> showImageReminder(appSettings.reminderContent)
            ReminderType.AUDIO -> playAudioReminder(appSettings.reminderContent)
            ReminderType.VIDEO -> showVideoReminder(appSettings.reminderContent)
        }
    }

    private fun showTextReminder(content: String) {
        try {
            Log.d("ReminderManager", "尝试显示文本提醒")
            
            // 检查悬浮窗权限
            if (!Settings.canDrawOverlays(context)) {
                Log.e("ReminderManager", "没有悬浮窗权限，回退到Toast")
                Toast.makeText(context.applicationContext, content, Toast.LENGTH_LONG).show()
                return
            }

            // 移除现有的提醒视图
            removeReminderView()
            
            // 获取WindowManager
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            Log.d("ReminderManager", "获取到WindowManager")
            
            // 创建提醒视图
            val inflater = LayoutInflater.from(context)
            reminderView = inflater.inflate(R.layout.reminder_popup, null)
            Log.d("ReminderManager", "创建提醒视图")

            reminderView?.apply {
                // 设置文本内容
                findViewById<TextView>(R.id.reminderText).text = content
                
                // 设置关闭按钮
                findViewById<View>(R.id.closeButton).setOnClickListener {
                    removeReminderView()
                }
            }

            // 创建布局参数
            val params = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.CENTER
            }

            Log.d("ReminderManager", "准备添加提醒视图到窗口")
            // 显示提醒视图
            windowManager?.addView(reminderView, params)
            Log.d("ReminderManager", "成功添加提醒视图")
            
            // 3秒后自动移除
            Handler(Looper.getMainLooper()).postDelayed({
                removeReminderView()
            }, 3000)
            
        } catch (e: Exception) {
            Log.e("ReminderManager", "显示提醒失败", e)
            // 出错时回退到Toast
            Toast.makeText(context.applicationContext, content, Toast.LENGTH_LONG).show()
        }
    }

    private fun removeReminderView() {
        try {
            Log.d("ReminderManager", "尝试移除提醒视图")
            reminderView?.let { view ->
                windowManager?.removeView(view)
                reminderView = null
                Log.d("ReminderManager", "成功移除提醒视图")
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "移除提醒视图失败", e)
        }
    }

    private fun showImageReminder(imagePath: String) {
        try {
            Toast.makeText(context.applicationContext, "图片提醒: $imagePath", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ReminderManager", "显示图片提醒失败", e)
        }
    }

    private fun playAudioReminder(audioPath: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                start()
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
            }
            Toast.makeText(context.applicationContext, "正在播放音频提醒", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ReminderManager", "播放音频提醒失败", e)
        }
    }

    private fun showVideoReminder(videoPath: String) {
        try {
            Toast.makeText(context.applicationContext, "视频提醒: $videoPath", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ReminderManager", "显示视频提醒失败", e)
        }
    }

    fun showTimeoutReminder(settings: AppSettings) {
        showTextReminder(
            settings.timeoutMessage.ifEmpty { 
                "你已经使用该应用${settings.timeLimit}分钟了" 
            }
        )
    }

    fun resetReminder() {
        Log.d("ReminderManager", "重置提醒状态")
        hasShownReminder = false
        removeReminderView()
    }
    
    // 辅助函数：获取当前Activity
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}