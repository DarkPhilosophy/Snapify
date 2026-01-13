package ro.snapify.ui.components

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ro.snapify.data.entity.MediaItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaInfoDialog(
    mediaItem: MediaItem,
    onKeep: () -> Unit,
    onUnkeep: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val listState = rememberLazyListState()

    val mediaType = remember(mediaItem.fileName) {
        getMediaType(mediaItem.fileName)
    }

    val resolution = remember(mediaItem.filePath) {
        getMediaResolution(mediaItem.filePath)
    }

    val infoItems = listOf(
        "ID" to mediaItem.id.toString(),
        "File Name" to mediaItem.fileName,
        "Media Type" to mediaType,
        "Resolution" to resolution,
        "File Path" to mediaItem.filePath,
        "File Size" to formatFileSize(mediaItem.fileSize),
        "Created At" to dateFormat.format(Date(mediaItem.createdAt)),
        "Deletion Timestamp" to (mediaItem.deletionTimestamp?.let { dateFormat.format(Date(it)) } ?: "Not set"),
        "Is Kept" to if (mediaItem.isKept) "Yes" else "No",
        "Content URI" to (mediaItem.contentUri ?: "Not available"),
        "Thumbnail Path" to (mediaItem.thumbnailPath ?: "Not generated (on-demand)"),
        "Deletion Work ID" to (mediaItem.deletionWorkId ?: "Not scheduled"),
    )

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title
                Text(
                    text = "Media Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                )

                // Scrollable content
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(infoItems) { (label, value) ->
                            InfoRow(label, value)
                        }
                    }
                    CustomScrollbar(
                        listState = listState,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }

                // Buttons at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            if (mediaItem.isKept) onUnkeep() else onKeep()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (mediaItem.isKept) "Unkeep" else "Keep",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    }
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f),
        )
    }
}

private fun getMediaType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> "Video"
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff" -> "Image"
        else -> "Unknown"
    }
}

private fun getMediaResolution(filePath: String): String {
    return try {
        val file = File(filePath)
        if (!file.exists()) return "File not found"

        val extension = file.name.substringAfterLast('.', "").lowercase()
        when {
            extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff") -> {
                // For images
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(filePath, options)
                if (options.outWidth > 0 && options.outHeight > 0) {
                    "${options.outWidth} x ${options.outHeight}"
                } else {
                    "Unknown"
                }
            }
            extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm") -> {
                // For videos
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                retriever.release()
                if (width > 0 && height > 0) {
                    "$width x $height"
                } else {
                    "Unknown"
                }
            }
            else -> "Not applicable"
        }
    } catch (e: Exception) {
        "Error retrieving"
    }
}

@Composable
private fun CustomScrollbar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollbarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val thumbHeightFraction by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size
            if (totalItems > 0 && visibleItems > 0) {
                visibleItems.toFloat() / totalItems.toFloat()
            } else {
                1f
            }
        }
    }

    val scrollProgress by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size
            if (totalItems > visibleItems) {
                listState.firstVisibleItemIndex.toFloat() / (totalItems - visibleItems).toFloat()
            } else {
                0f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxHeight().width(4.dp)) {
        val trackHeight = size.height
        val thumbHeight = trackHeight * thumbHeightFraction
        val maxThumbOffset = trackHeight - thumbHeight
        val thumbOffset = scrollProgress * maxThumbOffset

        drawRect(
            color = scrollbarColor,
            topLeft = Offset(0f, thumbOffset),
            size = Size(size.width, thumbHeight),
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return "%.2f %s".format(size, units[unitIndex])
}
