package com.example.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val packageName: String,
    val isEnabled: Boolean = false,
    val reminderType: ReminderType = ReminderType.TEXT,
    val reminderContent: String = "",
    val timeLimit: Int = 30,
    val timeoutMessage: String = ""
)

enum class ReminderType {
    TEXT,    // 文本
    IMAGE,   // 图片
    AUDIO,   // 音频
    VIDEO    // 视频
}