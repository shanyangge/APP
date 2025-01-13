package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.AppInfo
import com.example.app.domain.model.ReminderType
import com.example.app.ui.components.AppListItem
import com.example.app.ui.components.AppSettingsDialog
import com.example.app.ui.viewmodels.AppListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onPickFile: (ReminderType) -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    var showDialog by remember { mutableStateOf<AppInfo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(
                items = apps,
                key = { app -> app.packageName }
            ) { app ->
                AppListItem(
                    appInfo = app,
                    onAppSelected = { viewModel.toggleAppSelection(app.packageName) },
                    onSettingsClick = { showDialog = app }
                )
            }
        }

        // 显示设置对话框
        showDialog?.let { appInfo ->
            AppSettingsDialog(
                appInfo = appInfo,
                onDismiss = { showDialog = null },
                onPickFile = onPickFile
            )
        }
    }
}