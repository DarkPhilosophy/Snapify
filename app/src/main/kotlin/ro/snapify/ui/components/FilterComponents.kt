package ro.snapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.snapify.R
import ro.snapify.data.model.ScreenshotTab
import ro.snapify.util.UriPathConverter

/**
 * Multi-select filter bar for screenshot tags (Marked, Kept, Unmarked)
 */
@Composable
fun TagFilterBar(
    selectedTags: Set<ScreenshotTab>,
    onTagSelectionChanged: (Set<ScreenshotTab>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            ScreenshotTab.MARKED to stringResource(R.string.tab_marked),
            ScreenshotTab.KEPT to stringResource(R.string.kept),
            ScreenshotTab.UNMARKED to stringResource(R.string.unmarked)
        ).forEach { (tag, label) ->
            FilterChip(
                selected = tag in selectedTags,
                onClick = {
                    val newSelection = if (tag in selectedTags) {
                        selectedTags - tag
                    } else {
                        selectedTags + tag
                    }
                    onTagSelectionChanged(newSelection)
                },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
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
    modifier: Modifier = Modifier
) {
    val (selectedUri, setSelectedUri) = remember { mutableStateOf<String?>(null) }
    
    // Show dialog for long-pressed folder
    if (selectedUri != null) {
        val resolvedPath = UriPathConverter.uriToFilePath(selectedUri) ?: selectedUri
        AlertDialog(
            onDismissRequest = { setSelectedUri(null) },
            title = { Text("Folder Information") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Display Name:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UriPathConverter.uriToDisplayName(selectedUri),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Resolved Path:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = resolvedPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { setSelectedUri(null) }) {
                    Text("Close")
                }
            }
        )
    }
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableUris.zip(availablePaths).forEach { (uri, path) ->
            val folderName = UriPathConverter.uriToDisplayName(uri)
            val isSelected = path in selectedPaths
            
            Row(
                modifier = Modifier
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
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
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = folderName,
                    color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
