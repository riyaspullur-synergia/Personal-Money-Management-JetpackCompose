package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.SectionCard
import app.riyaspullur.personalmoneymanagement.core.util.AppLanguage
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isAppLockEnabled: Boolean,
    themeMode: String,
    language: String,
    onAppLockChange: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onRecycleBinClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackupRestoreClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.settings_logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_choose_theme)) },
            text = {
                Column {
                    val options = listOf("SYSTEM", "LIGHT", "DARK")
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(option)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == option,
                                onClick = {
                                    onThemeChange(option)
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                text = when (option) {
                                    "SYSTEM" -> stringResource(R.string.settings_theme_system)
                                    "LIGHT" -> stringResource(R.string.settings_theme_light)
                                    "DARK" -> stringResource(R.string.settings_theme_dark)
                                    else -> option
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_choose_language)) },
            text = {
                Column {
                    AppLanguage.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(option.code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = language == option.code,
                                onClick = {
                                    onLanguageChange(option.code)
                                    showLanguageDialog = false
                                }
                            )
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = stringResource(R.string.settings_section_appearance)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_theme)) },
                        supportingContent = {
                            val themeLabel = when (themeMode) {
                                "SYSTEM" -> stringResource(R.string.settings_theme_system)
                                "LIGHT" -> stringResource(R.string.settings_theme_light)
                                "DARK" -> stringResource(R.string.settings_theme_dark)
                                else -> themeMode
                            }
                            Text(themeLabel)
                        },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showThemeDialog = true }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_language)) },
                        supportingContent = { Text(AppLanguage.fromCode(language).displayName) },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showLanguageDialog = true }
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.settings_section_security)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_app_lock)) },
                        supportingContent = { Text(stringResource(R.string.settings_app_lock_description)) },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = isAppLockEnabled, onCheckedChange = onAppLockChange)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.settings_section_data_management)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_recycle_bin)) },
                        supportingContent = { Text(stringResource(R.string.settings_recycle_bin_description)) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onRecycleBinClick() }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_backup_restore)) },
                        supportingContent = { Text(stringResource(R.string.backup_description)) },
                        leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onBackupRestoreClick() }
                    )
                }
            }

            item {
                SectionCard(title = stringResource(R.string.settings_section_account)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_profile)) },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onProfileClick() }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_help_support)) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onHelpSupportClick() }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showLogoutDialog = true }
                    )
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun SettingsScreenPreview() {
    PersonalMoneyManagemntTheme {
        SettingsScreen(
            isAppLockEnabled = true,
            themeMode = "SYSTEM",
            language = AppLanguage.Default.code,
            onAppLockChange = {},
            onThemeChange = {},
            onLanguageChange = {},
            onRecycleBinClick = {},
            onProfileClick = {},
            onHelpSupportClick = {},
            onLogoutClick = {}
        )
    }
}
