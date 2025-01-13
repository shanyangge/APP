package com.example.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.App
import com.example.app.data.dao.AppSettingsDao
import com.example.app.domain.model.AppSettings
import com.example.app.domain.model.ReminderType
import com.example.app.utils.FilePickerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSettingsViewModel : ViewModel() {
    private val context = App.instance.applicationContext
    private val dao: AppSettingsDao = App.instance.database.appSettingsDao()

    private val _settings = MutableStateFlow<Map<String, AppSettings>>(emptyMap())
    val settings: StateFlow<Map<String, AppSettings>> = _settings

    private val _selectedFilePath = MutableStateFlow<String>("")
    val selectedFilePath: StateFlow<String> = _selectedFilePath

    init {
        viewModelScope.launch {
            dao.getAllSettings().collect { settingsList ->
                _settings.value = settingsList.associateBy { it.packageName }
            }
        }
    }

    fun handleSelectedFile(uri: Uri, type: ReminderType) {
        viewModelScope.launch {
            val fileName = "reminder_${System.currentTimeMillis()}_${type.name.lowercase()}"
            val filePath = FilePickerHelper.copyFileToInternalStorage(
                context,
                uri,
                fileName
            )
            _selectedFilePath.value = filePath
        }
    }

    suspend fun getSettings(packageName: String): AppSettings {
        return withContext(Dispatchers.IO) {
            dao.getSettings(packageName) ?: AppSettings(
                packageName = packageName,
                isEnabled = false,
                reminderType = ReminderType.TEXT,
                reminderContent = "",
                timeLimit = 30,
                timeoutMessage = ""
            )
        }
    }

    fun saveSettings(
        packageName: String,
        reminderType: ReminderType,
        reminderContent: String,
        timeLimit: Int,
        timeoutMessage: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = AppSettings(
                packageName = packageName,
                isEnabled = true,
                reminderType = reminderType,
                reminderContent = reminderContent,
                timeLimit = timeLimit,
                timeoutMessage = timeoutMessage
            )
            dao.saveSettings(settings)
        }
    }
}