package ro.snapify.ui

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ro.snapify.BuildConfig
import ro.snapify.R
import ro.snapify.ui.theme.SnapifyTheme

/** Content of the stage drawer: identity header, customization, quick toggles, entries. */
@Composable
fun DrawerMenuPanel(
    settingsViewModel: SettingsViewModel,
    scope: CoroutineScope,
    onOpenSettings: () -> Unit,
    onManageFolders: () -> Unit,
) {
    val themeAccentArgb by settingsViewModel.themeAccent.collectAsStateWithLifecycle(initialValue = null)
    val themeCornerScale by settingsViewModel.themeCornerScale.collectAsStateWithLifecycle(initialValue = 1f)
    val tokens = SnapifyTheme.colors
    val spacing = SnapifyTheme.spacing

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
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
                    text = stringResource(R.string.app_name).uppercase() + " // MENU",
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
            AutoCleanupToggle(
                enabled = settingsViewModel.autoCleanupEnabled.collectAsStateWithLifecycle(
                    initialValue = false,
                ).value,
                onToggle = { enabled ->
                    scope.launch { settingsViewModel.setAutoCleanupEnabled(enabled) }
                },
            )
        }

        item {
            LiveVideoPreviewToggle(
                enabled = settingsViewModel.liveVideoPreviewEnabled.collectAsStateWithLifecycle(
                    initialValue = false,
                ).value,
                onToggle = { enabled ->
                    scope.launch { settingsViewModel.setLiveVideoPreviewEnabled(enabled) }
                },
            )
        }

        item {
            DrawerMenuEntry(
                icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = tokens.accent) },
                label = stringResource(R.string.manage_folders),
                onClick = onManageFolders,
            )
        }

        item {
            DrawerMenuEntry(
                icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = tokens.accent) },
                label = stringResource(R.string.settings),
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun DrawerMenuEntry(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SnapifyTheme.shapes.cardShape)
            .background(SnapifyTheme.colors.surfaceRaised)
            .clickable(onClick = onClick)
            .padding(SnapifyTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SnapifyTheme.spacing.md),
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = SnapifyTheme.colors.ink,
        )
    }
}
