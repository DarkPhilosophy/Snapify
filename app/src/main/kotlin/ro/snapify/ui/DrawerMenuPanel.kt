package ro.snapify.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ro.snapify.BuildConfig
import ro.snapify.R
import ro.snapify.ui.components.PermissionDialog
import ro.snapify.ui.theme.SnapifyTheme

/**
 * The drawer IS the full settings surface: appearance, language, behavior,
 * permissions and folder management live here, one tap from anywhere.
 */
@Composable
fun DrawerMenuPanel(
    settingsViewModel: SettingsViewModel,
    scope: CoroutineScope,
    onOpenSettings: () -> Unit,
    onManageFolders: () -> Unit,
) {
    val context = LocalContext.current
    val tokens = SnapifyTheme.colors
    val spacing = SnapifyTheme.spacing

    val themeAccentArgb by settingsViewModel.themeAccent.collectAsStateWithLifecycle(initialValue = null)
    val themeCornerScale by settingsViewModel.themeCornerScale.collectAsStateWithLifecycle(initialValue = 1f)
    val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle(
        initialValue = ro.snapify.ui.theme.ThemeMode.SYSTEM,
    )
    val language by settingsViewModel.language.collectAsStateWithLifecycle(initialValue = "en")
    val isManualMode by settingsViewModel.isManualMode.collectAsStateWithLifecycle(initialValue = false)
    val deletionTime by settingsViewModel.deletionTime.collectAsStateWithLifecycle(
        initialValue = 15 * 60 * 1000L,
    )
    val autoCleanupEnabled by settingsViewModel.autoCleanupEnabled.collectAsStateWithLifecycle(
        initialValue = false,
    )
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle(
        initialValue = true,
    )
    val liveVideoPreviewEnabled by settingsViewModel.liveVideoPreviewEnabled.collectAsStateWithLifecycle(
        initialValue = false,
    )
    var showPermissionDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // The toggle re-checks the actual permission state on next composition
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Column(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .background(tokens.accent, RoundedCornerShape(2.dp)),
                    ) {}
                    Text(
                        text = BuildConfig.APP_DISPLAY_NAME,
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.ink,
                    )
                }
                Text(
                    text = stringResource(R.string.settings).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.inkFaint,
                )
            }
        }

        item {
            ThemeCustomizationControls(
                selectedAccentArgb = themeAccentArgb,
                cornerScale = themeCornerScale,
                onAccentSelected = { settingsViewModel.setThemeAccent(it) },
                onCornerScaleChange = { settingsViewModel.setThemeCornerScale(it) },
            )
        }

        item {
            ThemeSelector(
                currentTheme = currentTheme,
                onThemeSelected = { settingsViewModel.setThemeMode(it) },
                onThemeChange = { },
            )
        }

        item {
            LanguageSelector(
                currentLanguage = language,
                onLanguageSelected = { lang ->
                    scope.launch {
                        settingsViewModel.setLanguage(lang)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            val intent = context.packageManager
                                .getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            (context as? android.app.Activity)?.finish()
                        }
                    }
                },
            )
        }

        item {
            ModeSelector(
                isManualMode = isManualMode,
                onModeChanged = { manual ->
                    scope.launch { settingsViewModel.setManualMode(manual) }
                },
                currentTime = deletionTime,
                onTimeSelected = { time ->
                    scope.launch { settingsViewModel.setDeletionTime(time) }
                },
            )
        }

        item {
            AutoCleanupToggle(
                enabled = autoCleanupEnabled,
                onToggle = { enabled ->
                    scope.launch { settingsViewModel.setAutoCleanupEnabled(enabled) }
                },
            )
        }

        item {
            NotificationToggle(
                enabled = notificationsEnabled,
                onToggle = { enabled ->
                    scope.launch { settingsViewModel.setNotificationsEnabled(enabled) }
                },
                onRequestPermission = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
            )
        }

        item {
            LiveVideoPreviewToggle(
                enabled = liveVideoPreviewEnabled,
                onToggle = { enabled ->
                    scope.launch { settingsViewModel.setLiveVideoPreviewEnabled(enabled) }
                },
            )
        }

        item {
            PermissionsSection(onOpenPermissions = { showPermissionDialog = true })
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SnapifyTheme.shapes.cardShape)
                    .background(tokens.surfaceRaised)
                    .clickable(onClick = onManageFolders)
                    .padding(spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = tokens.accent)
                Text(
                    text = stringResource(R.string.manage_folders),
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                )
            }
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            autoCloseWhenGranted = false,
        )
    }
}
