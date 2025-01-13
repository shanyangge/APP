package com.example.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.app.domain.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListItem(
    appInfo: AppInfo,
    onAppSelected: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onSettingsClick) // 整个区域可点击，触发设置
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用图标
            Image(
                painter = rememberAsyncImagePainter(appInfo.icon),
                contentDescription = "应用图标",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            // 应用信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 只保留开关按钮
            Switch(
                checked = appInfo.isSelected,
                onCheckedChange = onAppSelected,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}