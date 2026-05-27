package com.example.lifemap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifemap.ui.SettingsViewModel
import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    themePreference: Int,
    onThemeChanged: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("lifemap_settings", Context.MODE_PRIVATE) }

    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", false))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsEnabled = isGranted
        sharedPrefs.edit { putBoolean("notifications_enabled", isGranted) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Impostazioni",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = if (isDarkTheme) Color.Black else Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Personalizzazione",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            ThemeSelectionSection(
                currentPreference = themePreference,
                onPreferenceChange = onThemeChanged
            )

            Text(
                text = "Dispositivo ed Avvisi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsRowCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SettingsLabel(
                        icon = Icons.Default.Notifications,
                        title = "Notifiche Anniversari",
                        subtitle = "Ricevi avvisi per\n i tuoi ricordi preferiti"
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationsEnabled = enabled
                                sharedPrefs.edit { putBoolean("notifications_enabled", enabled) }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val logoutColor = if (isDarkTheme) Color(0xFFFFB4AB) else Color(0xFFA51D24)

            SettingsRowCard(onClick = {
                vm.logout {
                    onLogoutClick()
                }}) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = logoutColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Disconnetti",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = logoutColor
                        )
                        Text(
                            text = "Esci dal tuo account attuale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRowCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        content = { content() }
    )
}

@Composable
fun SettingsLabel(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ThemeSelectionSection(
    currentPreference: Int,
    onPreferenceChange: (Int) -> Unit
) {
    val options = listOf(
        0 to "Auto",
        1 to "Chiaro",
        2 to "Scuro"
    )

    SettingsRowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsLabel(
                icon = Icons.Default.DarkMode,
                title = "Tema dell'app",
                subtitle = "Scegli l'aspetto visivo"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(4.dp)
            ) {
                options.forEach { (value, label) ->
                    val isSelected = currentPreference == value

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = tween(durationMillis = 300),
                        label = "backgroundColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(backgroundColor)
                            .clickable { onPreferenceChange(value) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
