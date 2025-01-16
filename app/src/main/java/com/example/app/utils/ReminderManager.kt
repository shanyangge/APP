package com.example.app.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.app.domain.model.AppSettings
import com.example.app.domain.model.ReminderType

class ReminderManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun showReminder(appSettings: AppSettings) {
        when (appSettings.reminderType) {
            ReminderType.TEXT -> showTextReminder(appSettings.reminderContent)
            ReminderType.IMAGE -> showImageReminder(appSettings.reminderContent)
            ReminderType.AUDIO -> playAudioReminder(appSettings.reminderContent)
            ReminderType.VIDEO -> showVideoReminder(appSettings.reminderContent)
        }
    }

    private fun showTextReminder(content: String) {
        try {
            Toast.makeText(context.applicationContext, content, Toast.LENGTH_LONG).show()
            Log.d("ReminderManager", "Toast 显示成功")
        } catch (e: Exception) {
            Log.e("ReminderManager", "显示提醒失败", e)
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
}