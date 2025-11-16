package ro.snapify.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ro.snapify.util.DebugLogger
import ro.snapify.util.PermissionUtils.updatePermissionStatuses

/**
 * Dedicated overlay permission request dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionOverlayDialog(
    onDismiss: () -> Unit,
    onPermissionGranted: (() -> Unit)? = null,
    onPermissionDenied: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    // Launcher for overlay permission (opens settings)
    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if permission was granted after returning from settings
        updatePermissionStatuses(
            context,
            listOf("overlay")
        ) { statusMap ->
            val isGranted = statusMap["overlay"] == true
            DebugLogger.info("PermissionOverlay", "Overlay permission result: $isGranted")

            if (isGranted) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDenied?.invoke()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val isOLED = MaterialTheme.colorScheme.surface == Color.Black
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOLED) Color.Black else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // Title
                    Text(
                        text = "Overlay Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Description
                    Text(
                        text = "To show the quick action overlay when you take screenshots, the app needs permission to draw over other apps. This allows instant access to keep or delete options.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Features list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "• Instant screenshot decisions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Quick keep/delete actions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• No need to open the app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip for Now")
                        }

                        androidx.compose.material3.Button(
                            onClick = {
                                hasRequestedPermission = true
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                overlayLauncher.launch(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Grant Permission")
                        }
                    }

                    // Note
                    Text(
                        text = "You can change this later in Settings > Apps > [App Name] > Display over other apps",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Simple overlay permission request function
 * Returns true if permission is granted, false otherwise
 */
@Composable
fun requestOverlayPermission(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onDismissed: () -> Unit = {}
): Boolean {
    var showDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Check current permission status
    updatePermissionStatuses(
        context,
        listOf("overlay")
    ) { statusMap ->
        val isGranted = statusMap["overlay"] == true
        if (isGranted) {
            permissionGranted = true
            onGranted()
        } else {
            showDialog = true
        }
    }

    if (showDialog) {
        PermissionOverlayDialog(
            onDismiss = {
                showDialog = false
                onDismissed()
            },
            onPermissionGranted = {
                showDialog = false
                permissionGranted = true
                onGranted()
            },
            onPermissionDenied = {
                showDialog = false
                onDenied()
            }
        )
    }

    return permissionGranted
}

/**
 * Overlay permission status checker
 */
@Composable
fun checkOverlayPermission(): Boolean {
    var isGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    updatePermissionStatuses(
        context,
        listOf("overlay")
    ) { statusMap ->
        isGranted = statusMap["overlay"] == true
    }

    return isGranted
}
