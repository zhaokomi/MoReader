package com.mochen.reader.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mochen.reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_appearance))
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = when (uiState.darkMode) {
                        "system" -> stringResource(R.string.settings_dark_mode_follow_system)
                        "light" -> stringResource(R.string.settings_dark_mode_off)
                        "dark" -> stringResource(R.string.settings_dark_mode_on)
                        else -> stringResource(R.string.settings_dark_mode_follow_system)
                    },
                    onClick = { showDarkModeDialog = true }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Outlined.Palette,
                    title = "动态配色",
                    subtitle = "跟随壁纸变色",
                    checked = uiState.dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = when (uiState.language) {
                        "system" -> "跟随系统"
                        "zh" -> "简体中文"
                        "en" -> "English"
                        else -> "跟随系统"
                    },
                    onClick = { showLanguageDialog = true }
                )
            }

            // Security section
            item {
                SettingsSectionHeader(title = "安全")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.settings_auto_lock),
                    subtitle = if (uiState.appLockEnabled) uiState.appLockType.uppercase() else stringResource(R.string.settings_auto_lock_off),
                    checked = uiState.appLockEnabled,
                    onCheckedChange = { viewModel.setAppLockEnabled(it) }
                )
            }

            // Data section
            item {
                SettingsSectionHeader(title = "数据")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Backup,
                    title = stringResource(R.string.backup_export),
                    subtitle = "导出书架数据、进度、书签和笔记",
                    onClick = { /* TODO: Implement export */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Restore,
                    title = stringResource(R.string.backup_import),
                    subtitle = "从备份文件恢复数据",
                    onClick = { /* TODO: Implement import */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.DeleteSweep,
                    title = stringResource(R.string.cache_manage),
                    subtitle = "缓存大小: ${uiState.cacheSize}",
                    onClick = { /* TODO: Implement cache management */ }
                )
            }

            // About section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_about))
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_version),
                    subtitle = "1.0.0",
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Feedback,
                    title = stringResource(R.string.about_feedback),
                    subtitle = "提交问题或建议",
                    onClick = { /* TODO: Implement feedback */ }
                )
            }
        }

        // Dark mode dialog
        if (showDarkModeDialog) {
            AlertDialog(
                onDismissRequest = { showDarkModeDialog = false },
                title = { Text(stringResource(R.string.settings_dark_mode)) },
                text = {
                    Column {
                        listOf(
                            "system" to stringResource(R.string.settings_dark_mode_follow_system),
                            "light" to stringResource(R.string.settings_dark_mode_off),
                            "dark" to stringResource(R.string.settings_dark_mode_on)
                        ).forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setDarkMode(mode)
                                        showDarkModeDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.darkMode == mode,
                                    onClick = {
                                        viewModel.setDarkMode(mode)
                                        showDarkModeDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Language dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(stringResource(R.string.settings_language)) },
                text = {
                    Column {
                        listOf(
                            "system" to "跟随系统",
                            "zh" to "简体中文",
                            "en" to "English"
                        ).forEach { (lang, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.language == lang,
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // About dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("关于墨阅") },
                text = {
                    Column {
                        Text("墨阅 MoReader")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("版本: 1.0.0")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("一款优雅的本地小说阅读器，支持 TXT、EPUB、MOBI、AZW3、PDF 等多种格式。")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.outline) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.outline) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
