package com.example.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.AppInfo
import com.example.app.domain.model.ReminderType
import com.example.app.ui.viewmodels.AppSettingsViewModel

@Composable
fun AppSettingsDialog(
    appInfo: AppInfo,
    onDismiss: () -> Unit,
    onPickFile: (ReminderType) -> Unit,
    viewModel: AppSettingsViewModel = viewModel()
) {
    var reminderType by remember { mutableStateOf<ReminderType>(ReminderType.TEXT) }
    var reminderContent by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("30") }
    var timeoutMessage by remember { mutableStateOf("") }

    // 加载已保存的设置
    LaunchedEffect(appInfo.packageName) {
        val settings = viewModel.getSettings(appInfo.packageName)
        reminderType = settings.reminderType
        reminderContent = settings.reminderContent
        timeLimit = settings.timeLimit.toString()
        timeoutMessage = settings.timeoutMessage
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("应用设置 - ${appInfo.appName}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 提醒类型选择
                Text("提醒类型", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReminderType.values().forEach { type ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(
                                selected = reminderType == type,
                                onClick = { reminderType = type }
                            )
                            Text(
                                when(type) {
                                    ReminderType.TEXT -> "文本"
                                    ReminderType.IMAGE -> "图片"
                                    ReminderType.AUDIO -> "音频"
                                    ReminderType.VIDEO -> "视频"
                                }
                            )
                        }
                    }
                }

                // 提醒内容
                when (reminderType) {
                    ReminderType.TEXT -> {
                        OutlinedTextField(
                            value = reminderContent,
                            onValueChange = { reminderContent = it },
                            label = { Text("提醒内容") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        Column {
                            Button(
                                onClick = { onPickFile(reminderType) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("选择${
                                    when(reminderType) {
                                        ReminderType.IMAGE -> "图片"
                                        ReminderType.AUDIO -> "音频"
                                        ReminderType.VIDEO -> "视频"
                                        else -> ""
                                    }
                                }")
                            }
                            // 显示已选择的文件路径
                            viewModel.selectedFilePath.collectAsState().value.let { path ->
                                if (path.isNotEmpty()) {
                                    Text(
                                        text = "已选择文件：${path.substringAfterLast("/")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 使用时间限制
                OutlinedTextField(
                    value = timeLimit,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            timeLimit = it
                        }
                    },
                    label = { Text("使用时间限制（分钟）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 超时提醒消息
                OutlinedTextField(
                    value = timeoutMessage,
                    onValueChange = { timeoutMessage = it },
                    label = { Text("超时提醒消息") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.saveSettings(
                        packageName = appInfo.packageName,
                        reminderType = reminderType,
                        reminderContent = if (reminderType == ReminderType.TEXT) {
                            reminderContent
                        } else {
                            viewModel.selectedFilePath.value
                        },
                        timeLimit = timeLimit.toIntOrNull() ?: 30,
                        timeoutMessage = timeoutMessage
                    )
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}