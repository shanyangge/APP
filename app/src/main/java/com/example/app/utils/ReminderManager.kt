package com.example.app.utils

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
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
        Dialog(context).apply {
            setContentView(
                ComposeView(context).apply {
                    setContent {
                        TextReminderContent(
                            content = content,
                            onDismiss = { dismiss() }
                        )
                    }
                }
            )
            show()
        }
    }

    @Composable
    private fun TextReminderContent(
        content: String,
        onDismiss: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("我知道了")
                }
            }
        }
    }

    private fun showImageReminder(imagePath: String) {
        Dialog(context).apply {
            val imageView = ImageView(context).apply {
                setImageURI(Uri.parse(imagePath))
            }
            setContentView(imageView)
            show()
        }
    }

    private fun playAudioReminder(audioPath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioPath)
            prepare()
            start()
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
        }
    }

    private fun showVideoReminder(videoPath: String) {
        Dialog(context).apply {
            val videoView = VideoView(context).apply {
                setVideoURI(Uri.parse(videoPath))
                start()
            }
            setContentView(videoView)
            show()
        }
    }

    fun showTimeoutReminder(settings: AppSettings) {
        Dialog(context).apply {
            setContentView(
                ComposeView(context).apply {
                    setContent {
                        TextReminderContent(
                            content = settings.timeoutMessage.ifEmpty { 
                                "你已经使用该应用${settings.timeLimit}分钟了" 
                            },
                            onDismiss = { dismiss() }
                        )
                    }
                }
            )
            show()
        }
    }
}