package com.puntofacil.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puntofacil.ui.theme.*

data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

data class SettingSection(
    val title: String,
    val items: List<SettingItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    // Show error message
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // In a real app, you might want to show a Snackbar
            viewModel.clearError()
        }
    }

    val settingSections = listOf(
        SettingSection(
            title = "Business",
            items = listOf(
                SettingItem(
                    title = "Business Information",
                    subtitle = "Store name, address, contact",
                    icon = Icons.Default.Business,
                    onClick = { /* Navigate to business info */ }
                ),
                SettingItem(
                    title = "Tax Settings",
                    subtitle = "Configure tax rates",
                    icon = Icons.Default.Receipt,
                    onClick = { /* Navigate to tax settings */ }
                ),
                SettingItem(
                    title = "Receipt Settings",
                    subtitle = "Customize receipt format",
                    icon = Icons.Default.Print,
                    onClick = { /* Navigate to receipt settings */ }
                )
            )
        ),
        SettingSection(
            title = "User Management",
            items = listOf(
                SettingItem(
                    title = "User Profile",
                    subtitle = "Edit your profile information",
                    icon = Icons.Default.Person,
                    onClick = { /* Navigate to profile */ }
                ),
                SettingItem(
                    title = "Change Password",
                    subtitle = "Update your password",
                    icon = Icons.Default.Lock,
                    onClick = { /* Navigate to change password */ }
                ),
                SettingItem(
                    title = "Manage Users",
                    subtitle = "Add, edit, or remove users",
                    icon = Icons.Default.People,
                    onClick = { /* Navigate to user management */ }
                )
            )
        ),
        SettingSection(
            title = "Data & Backup",
            items = listOf(
                SettingItem(
                    title = "Backup Data",
                    subtitle = "Create a backup of your data",
                    icon = Icons.Default.Backup,
                    onClick = { viewModel.createBackup() }
                ),
                SettingItem(
                    title = "Restore Data",
                    subtitle = "Restore from a backup",
                    icon = Icons.Default.Restore,
                    onClick = { /* Navigate to restore */ }
                ),
                SettingItem(
                    title = "Export Data",
                    subtitle = "Export data to CSV",
                    icon = Icons.Default.FileDownload,
                    onClick = { viewModel.exportData() }
                )
            )
        ),
        SettingSection(
            title = "App Settings",
            items = listOf(
                SettingItem(
                    title = "Notifications",
                    subtitle = "Configure app notifications",
                    icon = Icons.Default.Notifications,
                    onClick = { /* Navigate to notifications */ }
                ),
                SettingItem(
                    title = "Theme",
                    subtitle = "Light or dark mode",
                    icon = Icons.Default.Palette,
                    onClick = { /* Navigate to theme settings */ }
                ),
                SettingItem(
                    title = "Language",
                    subtitle = "Change app language",
                    icon = Icons.Default.Language,
                    onClick = { /* Navigate to language settings */ }
                )
            )
        ),
        SettingSection(
            title = "Support",
            items = listOf(
                SettingItem(
                    title = "Help & Support",
                    subtitle = "Get help and contact support",
                    icon = Icons.Default.Help,
                    onClick = { /* Navigate to help */ }
                ),
                SettingItem(
                    title = "About",
                    subtitle = "App version and information",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                ),
                SettingItem(
                    title = "Privacy Policy",
                    subtitle = "View privacy policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { /* Navigate to privacy policy */ }
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            settingSections.forEach { section ->
                item {
                    SettingSectionCard(section = section)
                }
            }
            
            // Logout Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Logout",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Sign out of your account",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // Show loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun SettingSectionCard(
    section: SettingSection
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Section Header
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )
            
            // Section Items
            section.items.forEach { item ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = item.subtitle?.let {
                        {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (item != section.items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "PuntoFácil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A simple and efficient point of sale application for small businesses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Close")
                }
            }
        }
    }
}