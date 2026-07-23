package ro.snapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.snapify.R
import ro.snapify.ui.theme.SnapifyTheme
import ro.snapify.data.model.ScreenshotTab
import ro.snapify.util.UriPathConverter

/**
 * Multi-select filter bar for screenshot tags (Marked, Kept, Unmarked)
 */
@Composable
fun TagFilterBar(
    selectedTags: Set<ScreenshotTab>,
    onTagSelectionChanged: (Set<ScreenshotTab>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = SnapifyTheme.colors
    val spacing = SnapifyTheme.spacing

    Surface(
        modifier = modifier
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        shape = SnapifyTheme.shapes.pillShape,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                ScreenshotTab.MARKED to stringResource(R.string.tab_marked),
                ScreenshotTab.KEPT to stringResource(R.string.kept),
                ScreenshotTab.UNMARKED to stringResource(R.string.unmarked),
            ).forEach { (tag, label) ->
                val isSelected = tag in selectedTags
                Box(
                    modifier = Modifier
                        .clip(SnapifyTheme.shapes.pillShape)
                        .background(
                            if (isSelected) tokens.accentSoft else Color.Transparent,
                        )
                        .clickable {
                            val newSelection = if (isSelected) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                            onTagSelectionChanged(newSelection)
                        }
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                ) {
                    Text(
                        text = label.uppercase(),
                        color = if (isSelected) tokens.accent else tokens.inkSoft,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * Multi-select filter bar for folders
 */
@Composable
fun FolderFilterBar(
    availableUris: List<String>,
    availablePaths: List<String>,
    selectedPaths: Set<String>,
    onFolderSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (selectedUri, setSelectedUri) = remember { mutableStateOf<String?>(null) }

    // Show dialog for long-pressed folder
    if (selectedUri != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val resolvedPath = UriPathConverter.uriToFilePath(selectedUri, context) ?: selectedUri
        val displayName = UriPathConverter.uriToDisplayName(selectedUri, context)

        AlertDialog(
            onDismissRequest = { setSelectedUri(null) },
            shape = SnapifyTheme.shapes.dialogShape,
            title = {
                Text(
                    "Folder Information",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Original URI
                    Text(
                        text = "Original URI:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = selectedUri,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                SnapifyTheme.colors.surfaceRaised,
                                shape = SnapifyTheme.shapes.fieldShape,
                            )
                            .padding(8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Display Name
                    Text(
                        text = "Display Name:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Resolved Path
                    Text(
                        text = "Resolved Path:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = resolvedPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                SnapifyTheme.colors.surfaceRaised,
                                shape = SnapifyTheme.shapes.fieldShape,
                            )
                            .padding(8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { setSelectedUri(null) }) {
                    Text(
                        "Close",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        availableUris.zip(availablePaths).forEach { (uri, path) ->
            val folderName = UriPathConverter.uriToDisplayName(uri, context)
            val isSelected = path in selectedPaths

            val tokens = SnapifyTheme.colors
            Row(
                modifier = Modifier
                    .clip(SnapifyTheme.shapes.pillShape)
                    .background(
                        color = if (isSelected) tokens.accentSoft else tokens.surfaceRaised,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) tokens.accent else tokens.hairline,
                        shape = SnapifyTheme.shapes.pillShape,
                    )
                    .combinedClickable(
                        onClick = {
                            val newSelection = if (isSelected) {
                                selectedPaths - path
                            } else {
                                selectedPaths + path
                            }
                            onFolderSelectionChanged(newSelection)
                        },
                        onLongClick = {
                            setSelectedUri(uri)
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = folderName,
                    color = if (isSelected) tokens.accent else tokens.inkSoft,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
