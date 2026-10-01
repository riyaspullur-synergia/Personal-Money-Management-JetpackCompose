package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.core.ui.components.SectionCard
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The ViewModel keeps a small, fixed set of English error literals (unchanged, to stay
 * backward compatible with existing ViewModel tests). This maps the known ones to a
 * localized string at the UI boundary; anything unrecognized (e.g. a raw exception message)
 * is shown as-is since it isn't a designed, translatable piece of UI copy.
 */
@Composable
private fun localizedProfileError(message: String): String = when (message) {
    "User not found" -> stringResource(R.string.profile_error_user_not_found)
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onUpdateClick: (String) -> Unit,
    onChangePasswordClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { old, new ->
                onChangePasswordClick(old, new)
                showPasswordDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (!isEditMode && uiState is ProfileUiState.Success) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.profile_edit_content_description))
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is ProfileUiState.Loading -> LoadingScreen(modifier = Modifier.padding(padding))
            is ProfileUiState.Success -> {
                ProfileContent(
                    user = uiState.user,
                    isEditMode = isEditMode,
                    onUpdateClick = {
                        onUpdateClick(it)
                        isEditMode = false
                    },
                    onCancelClick = { isEditMode = false },
                    onChangePasswordClick = { showPasswordDialog = true },
                    modifier = Modifier.padding(padding)
                )
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = localizedProfileError(uiState.message), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    isEditMode: Boolean,
    onUpdateClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember(user.displayName) { mutableStateOf(user.displayName) }
    val joinDate = remember(user.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(user.createdAt))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isEditMode) {
            SectionCard(title = stringResource(R.string.profile_edit_title), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.profile_display_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                    Button(
                        onClick = { onUpdateClick(displayName) },
                        modifier = Modifier.weight(1f),
                        enabled = displayName.isNotBlank() && displayName != user.displayName
                    ) {
                        Text(stringResource(R.string.common_save))
                    }
                }
            }
        } else {
            SectionCard(title = stringResource(R.string.profile_account_info_title), modifier = Modifier.fillMaxWidth()) {
                ProfileInfoItem(label = stringResource(R.string.profile_display_name_label), value = user.displayName)
                ProfileInfoItem(label = stringResource(R.string.profile_username_label), value = user.username)
                ProfileInfoItem(label = stringResource(R.string.profile_member_since_label), value = joinDate, showDivider = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onChangePasswordClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_change_password))
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_change_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text(stringResource(R.string.profile_current_password_label)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.profile_new_password_label)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.profile_confirm_new_password_label)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                            Text(stringResource(R.string.profile_passwords_do_not_match))
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword
            ) {
                Text(stringResource(R.string.profile_change_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun ProfileInfoItem(label: String, value: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp)
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun ProfileScreenPreview() {
    val mockUser = User(
        id = 1,
        username = "riyas",
        passwordHash = "",
        displayName = "Riyas Pullur",
        createdAt = System.currentTimeMillis()
    )
    PersonalMoneyManagemntTheme {
        ProfileScreen(
            uiState = ProfileUiState.Success(mockUser),
            onUpdateClick = {},
            onChangePasswordClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun ProfileScreenLoadingPreview() {
    PersonalMoneyManagemntTheme {
        ProfileScreen(
            uiState = ProfileUiState.Loading,
            onUpdateClick = {},
            onChangePasswordClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun ProfileScreenErrorPreview() {
    PersonalMoneyManagemntTheme {
        ProfileScreen(
            uiState = ProfileUiState.Error("Failed to load profile"),
            onUpdateClick = {},
            onChangePasswordClick = { _, _ -> },
            onBackClick = {}
        )
    }
}
