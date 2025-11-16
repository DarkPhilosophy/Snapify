package ro.snapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PictureControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRotate: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Zoom out
        IconButton(
            onClick = onZoomOut,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom out",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Zoom in
        IconButton(
            onClick = onZoomIn,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom in",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Rotate
        IconButton(
            onClick = onRotate,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = "Rotate",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Reset
        IconButton(
            onClick = onReset,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
