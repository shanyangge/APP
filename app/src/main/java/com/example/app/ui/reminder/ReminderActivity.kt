package com.example.app.ui.reminder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.app.databinding.FullscreenReminderLayoutBinding

class ReminderActivity : ComponentActivity() {
    private lateinit var binding: FullscreenReminderLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = FullscreenReminderLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取提醒内容
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: return finish()
        binding.reminderContent.text = content

        // 确认按钮点击事件
        binding.confirmButton.setOnClickListener {
            finish()
        }
    }

    companion object {
        private const val EXTRA_CONTENT = "extra_content"

        fun createIntent(context: Context, content: String): Intent {
            return Intent(context, ReminderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(EXTRA_CONTENT, content)
            }
        }
    }
}