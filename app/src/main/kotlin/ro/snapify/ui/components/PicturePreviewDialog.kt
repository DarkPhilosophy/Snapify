package ro.snapify.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ro.snapify.data.entity.MediaItem
import kotlin.math.max

// Constants for picture viewer
internal object PictureConstants {
    const val AVAILABLE_AREA_PERCENT = 0.95f
}

// Helper function to calculate picture display size
fun calculatePictureDisplaySize(
    imageAspectRatio: Float,
    availableWidthPx: Float,
    availableHeightPx: Float,
): Pair<Float, Float> = if (imageAspectRatio > availableWidthPx / availableHeightPx) {
    // Image is wider, fit to width
    availableWidthPx to (availableWidthPx / imageAspectRatio)
} else {
    // Image is taller, fit to height
    (availableHeightPx * imageAspectRatio) to availableHeightPx
}

@Composable
fun PicturePreviewDialog(
    mediaItem: MediaItem,
    position: Offset? = null,
    onDismiss: () -> Unit,
) {
    // Handle back press to dismiss dialog
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current

    // State
    var imageScale by remember { mutableFloatStateOf(1f) }
    var imageRotation by remember { mutableFloatStateOf(0f) }
    var imagePanX by remember { mutableFloatStateOf(0f) }
    var imagePanY by remember { mutableFloatStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }
    // UI always visible, no toggle

    val dialogScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            500,
            easing = androidx.compose.animation.core.EaseOutCubic,
        ),
        label = "dialogScale",
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            500,
            easing = androidx.compose.animation.core.EaseOutCubic,
        ),
        label = "dialogAlpha",
    )

    // Get image aspect ratio considering EXIF orientation
    val originalImageAspectRatio = remember(mediaItem) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(mediaItem.filePath, options)

        var width = options.outWidth
        var height = options.outHeight

        // Check EXIF orientation
        try {
            val exif = ExifInterface(mediaItem.filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )

            // If rotated 90 or 270 degrees, swap dimensions
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                orientation == ExifInterface.ORIENTATION_TRANSVERSE ||
                orientation == ExifInterface.ORIENTATION_TRANSPOSE
            ) {
                val temp = width
                width = height
                height = temp
            }
        } catch (_: Exception) {
            // Ignore EXIF errors
        }

        if (width > 0 && height > 0) {
            width.toFloat() / height.toFloat()
        } else {
            1f // fallback
        }
    }

    // For draggable dialog
    var windowOffsetX by remember { mutableFloatStateOf(0f) }
    var windowOffsetY by remember { mutableFloatStateOf(0f) }

    // Get system bars and display cutout insets for safe area calculation
    val systemBarsInsets = WindowInsets.systemBars
    val displayCutoutInsets = WindowInsets.displayCutout

    // Calculate safe area by subtracting insets from full screen
    val fullScreenWidthPx = configuration.screenWidthDp * density.density
    val fullScreenHeightPx = configuration.screenHeightDp * density.density

    val leftInsetPx = max(
        systemBarsInsets.getLeft(density, layoutDirection),
        displayCutoutInsets.getLeft(density, layoutDirection),
    )
    val topInsetPx = max(systemBarsInsets.getTop(density), displayCutoutInsets.getTop(density))
    val rightInsetPx = max(
        systemBarsInsets.getRight(density, layoutDirection),
        displayCutoutInsets.getRight(density, layoutDirection),
    )
    val bottomInsetPx =
        max(systemBarsInsets.getBottom(density), displayCutoutInsets.getBottom(density))

    // Safe area dimensions (excluding system UI and display cutouts)
    val availableScreenWidth = fullScreenWidthPx - leftInsetPx - rightInsetPx
    val availableScreenHeight = fullScreenHeightPx - topInsetPx - bottomInsetPx

    // Calculate available screen space as percentage of safe area
    val safeWidthDp = availableScreenWidth / density.density
    val safeHeightDp = availableScreenHeight / density.density
    val availableWidthDp =
        safeWidthDp * PictureConstants.AVAILABLE_AREA_PERCENT // 95% of safe area width
    val availableHeightDp =
        safeHeightDp * PictureConstants.AVAILABLE_AREA_PERCENT // 95% of safe area height

    // Calculate display size based on image aspect ratio and available space
    val availableWidthPx = availableWidthDp * density.density
    val availableHeightPx = availableHeightDp * density.density
    val (displayWidthPx, displayHeightPx) = calculatePictureDisplaySize(
        originalImageAspectRatio,
        availableWidthPx,
        availableHeightPx,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Position the dialog within safe area bounds
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .offset {
                        // Base position from click - allow positioning within safe area (accounting for insets)
                        val baseX = if (position != null) {
                            val preferredX = position.x - displayWidthPx / 2
                            // Offset by left inset and constrain within safe area
                            (preferredX - leftInsetPx).coerceIn(
                                0f,
                                max(0f, availableScreenWidth - displayWidthPx),
                            )
                        } else {
                            0f
                        }

                        val baseY = if (position != null) {
                            val preferredY = position.y - displayHeightPx / 2
                            // Offset by top inset and constrain within safe area
                            (preferredY - topInsetPx).coerceIn(
                                0f,
                                availableScreenHeight - displayHeightPx + topInsetPx.toFloat() + bottomInsetPx.toFloat(),
                            )
                        } else {
                            0f
                        }

                        // Add drag offset with bounds checking
                        val draggedX =
                            (baseX + windowOffsetX).coerceIn(
                                0f,
                                availableScreenWidth - displayWidthPx + leftInsetPx.toFloat() + rightInsetPx.toFloat(),
                            )
                        val draggedY = (baseY + windowOffsetY).coerceIn(
                            topInsetPx.toFloat(),
                            availableScreenHeight - displayHeightPx + topInsetPx.toFloat() + bottomInsetPx.toFloat(),
                        )

                        // Update offsets to stay within bounds
                        windowOffsetX = draggedX - baseX
                        windowOffsetY = draggedY - baseY

                        IntOffset(draggedX.toInt(), draggedY.toInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            // Only drag window if image is not zoomed
                            if (imageScale == 1f) {
                                change.consume()
                                windowOffsetX += dragAmount.x
                                windowOffsetY += dragAmount.y
                            }
                        }
                    }
                    .size(
                        width = (displayWidthPx / density.density).dp,
                        height = (displayHeightPx / density.density).dp,
                    )
                    .graphicsLayer(
                        scaleX = dialogScale,
                        scaleY = dialogScale,
                        alpha = dialogAlpha,
                    ),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color.Black,
                ),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp),
                        ),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaItem.contentUri ?: "file://${mediaItem.filePath}")
                            .build(),
                        contentDescription = mediaItem.fileName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotationDelta ->
                                    // If we are zoomed in, allow panning the internal image
                                    // If we are NOT zoomed (scale=1), the outer drag listener handles window movement.
                                    // However, detectTransformGestures consumes events.
                                    // We'll update the internal transform state.
                                    imageScale = (imageScale * zoom).coerceAtLeast(1f).coerceAtMost(3f)
                                    // Only rotate if zoomed out or intentionally rotating?
                                    // Let's allow rotation always for now as requested.
                                    imageRotation += rotationDelta

                                    // Pan logic for internal image
                                    if (imageScale > 1f) {
                                        // Update internal offset for panning INSIDE the zoomed view
                                        imagePanX += pan.x
                                        imagePanY += pan.y
                                    }
                                }
                            }
                            .graphicsLayer(
                                scaleX = imageScale,
                                scaleY = imageScale,
                                rotationZ = imageRotation,
                                translationX = imagePanX,
                                translationY = imagePanY,
                            )
                            .clickable {
                                controlsVisible = !controlsVisible
                            },
                    )

                    // Top bar
                    if (controlsVisible) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mediaItem.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(
                                onClick = {
                                    imageScale = 1f
                                    imageRotation = 0f
                                    imagePanX = 0f
                                    imagePanY = 0f
                                    windowOffsetX = 0f
                                    windowOffsetY = 0f
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    // Bottom bar
                    if (controlsVisible) {
                        PictureControls(
                            onZoomOut = { imageScale = (imageScale / 1.2f).coerceAtLeast(1f) },
                            onZoomIn = { imageScale = (imageScale * 1.2f).coerceAtMost(3f) },
                            onRotate = { imageRotation = (imageRotation + 90f) % 360f },
                            onReset = {
                                imageScale = 1f
                                imageRotation = 0f
                                imagePanX = 0f
                                imagePanY = 0f
                                windowOffsetX = 0f
                                windowOffsetY = 0f
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}
