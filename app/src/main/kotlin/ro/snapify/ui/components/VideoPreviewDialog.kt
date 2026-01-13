package ro.snapify.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ro.snapify.data.entity.MediaItem
import ro.snapify.util.DebugLogger
import kotlin.math.max
import kotlin.math.min
import androidx.compose.material3.MaterialTheme as MaterialTheme3

// Constants for dialog components
internal object DialogConstants {
    const val AVAILABLE_AREA_PERCENT = 0.95f
}

// Helper function to calculate video display size
fun calculateVideoDisplaySize(
    videoSize: IntSize?,
    availableWidthPx: Float,
    availableHeightPx: Float,
): Pair<Float, Float> = if (videoSize != null) {
    val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
    if (aspectRatio > availableWidthPx / availableHeightPx) {
        // Video is wider, fit to width
        availableWidthPx to (availableWidthPx / aspectRatio)
    } else {
        // Video is taller, fit to height
        (availableHeightPx * aspectRatio) to availableHeightPx
    }
} else {
    // Default size while loading
    kotlin.math.min(availableWidthPx, 300f) to kotlin.math.min(availableHeightPx, 200f)
}

// Helper function to calculate safe positioning
fun calculateSafePosition(
    position: Offset?,
    displayWidthPx: Float,
    displayHeightPx: Float,
    leftInsetPx: Float,
    topInsetPx: Float,
    availableScreenWidth: Float,
    availableScreenHeight: Float,
): Offset {
    val preferredX = position?.x?.minus(displayWidthPx / 2) ?: 0f
    val preferredY = position?.y?.minus(displayHeightPx / 2) ?: 0f

    val safeX =
        (preferredX - leftInsetPx).coerceIn(0f, max(0f, availableScreenWidth - displayWidthPx))
    val safeY = (preferredY - topInsetPx).coerceIn(0f, availableScreenHeight - displayHeightPx)

    return Offset(safeX, safeY)
}

/**
 * Video preview dialog with full controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPreviewDialog(
    mediaItem: MediaItem,
    position: androidx.compose.ui.geometry.Offset? = null,
    onDismiss: () -> Unit,
) {
    // Handle back press to dismiss dialog
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current

    // For draggable dialog
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // For debug logging
    var lastLoggedX by remember { mutableFloatStateOf(0f) }
    var lastLoggedY by remember { mutableFloatStateOf(0f) }

// State
    var rotation by remember { mutableFloatStateOf(0f) }
    var isFullscreen by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Save initial orientation and pause video when app loses focus
    val initialOrientation = remember {
        (context as? android.app.Activity)?.requestedOrientation
            ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    DisposableEffect(Unit) {
        // Set to sensor orientation for video viewing
        (context as? android.app.Activity)?.requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Note: Not hiding system UI for non-fullscreen video player

        onDispose {
            // Restore initial orientation
            (context as? android.app.Activity)?.requestedOrientation = initialOrientation
        }
    }

    val dialogScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "dialogScale",
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "dialogAlpha",
    )

    // Video player state
    var videoState by remember { mutableStateOf(VideoPlayerState()) }

    // Allow drawing over all system UI (status bar, navigation, camera cutouts)
    val localView = LocalView.current

    // Get system bars and display cutout insets
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

    DebugLogger.info(
        "VideoPreviewDialog",
        "Full screen: ${fullScreenWidthPx.toInt()}x${fullScreenHeightPx.toInt()} px",
    )
    DebugLogger.info(
        "VideoPreviewDialog",
        "Insets - L:$leftInsetPx T:$topInsetPx R:$rightInsetPx B:$bottomInsetPx",
    )
    DebugLogger.info(
        "VideoPreviewDialog",
        "Safe area: ${availableScreenWidth.toInt()}x${availableScreenHeight.toInt()} px",
    )
    DebugLogger.info(
        "VideoPreviewDialog",
        "Has camera cutout: ${if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) localView.rootWindowInsets?.displayCutout != null else false}",
    )

    // Calculate available screen space as percentage of safe area
    val safeWidthDp = availableScreenWidth / density.density
    val safeHeightDp = availableScreenHeight / density.density
    val availableWidthDp = safeWidthDp * 0.95f // 95% of safe area width
    val availableHeightDp = safeHeightDp * 0.95f // 95% of safe area height

    DebugLogger.info(
        "VideoPreviewDialog",
        "Available: ${availableWidthDp.toInt()}x${availableHeightDp.toInt()} dp",
    )

    // Video preview overlay in main window to draw behind cutouts
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(clip = false)
            .clickable(onClick = onDismiss),
    ) {
        // Get video size to calculate display size
        var videoSizeState by remember { mutableStateOf<androidx.compose.ui.unit.IntSize?>(null) }

        // Calculate actual display size based on video aspect ratio and available space
        val displayWidthPx: Float
        val displayHeightPx: Float
        if (videoSizeState != null) {
            val videoSize = videoSizeState!!
            val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
            val availableWidthPx = availableWidthDp * density.density
            val availableHeightPx = availableHeightDp * density.density

            DebugLogger.info(
                "VideoPreviewDialog",
                "Video size: ${videoSize.width}x${videoSize.height}, aspectRatio: $aspectRatio",
            )
            DebugLogger.info(
                "VideoPreviewDialog",
                "Available px: ${availableWidthPx.toInt()}x${availableHeightPx.toInt()}",
            )

            if (aspectRatio > availableWidthPx / availableHeightPx) {
                // Video is wider, fit to width
                displayWidthPx = availableWidthPx
                displayHeightPx = availableWidthPx / aspectRatio
                DebugLogger.info(
                    "VideoPreviewDialog",
                    "Fitting to width: ${displayWidthPx.toInt()}x${displayHeightPx.toInt()}",
                )
            } else {
                // Video is taller, fit to height
                displayHeightPx = availableHeightPx
                displayWidthPx = availableHeightPx * aspectRatio
                DebugLogger.info(
                    "VideoPreviewDialog",
                    "Fitting to height: ${displayWidthPx.toInt()}x${displayHeightPx.toInt()}",
                )
            }
        } else {
            // Default size while loading
            displayWidthPx = min(availableWidthDp * density.density, 300f * density.density)
            displayHeightPx = min(availableHeightDp * density.density, 200f * density.density)
            DebugLogger.info(
                "VideoPreviewDialog",
                "Default size: ${displayWidthPx.toInt()}x${displayHeightPx.toInt()}",
            )
        }

        // Account for border width in bounds calculation (controls are overlaid, don't extend container bounds)
        val borderWidthPx = 4f * density.density
        val effectiveWidthPx = displayWidthPx + borderWidthPx
        val effectiveHeightPx = displayHeightPx + borderWidthPx

        DebugLogger.info(
            "VideoPreviewDialog",
            "Display: ${displayWidthPx.toInt()}x${displayHeightPx.toInt()} px",
        )
        DebugLogger.info(
            "VideoPreviewDialog",
            "Effective: ${effectiveWidthPx.toInt()}x${effectiveHeightPx.toInt()} px",
        )

        // Get cutout dimensions for video extension
        val cutoutLeft = displayCutoutInsets.getLeft(density, layoutDirection)
        val cutoutTop = displayCutoutInsets.getTop(density)
        val cutoutRight = displayCutoutInsets.getRight(density, layoutDirection)
        val cutoutBottom = displayCutoutInsets.getBottom(density)

        Card(
            modifier = Modifier
                .graphicsLayer(clip = false)
                .offset {
                    // Base position from click - allow positioning within safe area (accounting for insets)
                    val baseX = if (position != null) {
                        val preferredX = position.x - displayWidthPx / 2
                        // Offset by left inset and constrain within safe area
                        (preferredX - leftInsetPx).coerceIn(
                            0f,
                            max(0f, availableScreenWidth - effectiveWidthPx),
                        )
                    } else {
                        0f
                    }

                    val baseY = if (position != null) {
                        val preferredY = position.y - displayHeightPx / 2
                        // Offset by top inset and constrain within safe area
                        (preferredY - topInsetPx).coerceIn(
                            0f,
                            availableScreenHeight - effectiveHeightPx + topInsetPx.toFloat() + bottomInsetPx.toFloat(),
                        )
                    } else {
                        0f
                    }

                    // Add drag offset with bounds checking
                    val draggedX =
                        (baseX + offsetX).coerceIn(
                            0f,
                            availableScreenWidth - effectiveWidthPx + leftInsetPx.toFloat() + rightInsetPx.toFloat(),
                        )
                    val draggedY = (baseY + offsetY).coerceIn(
                        topInsetPx.toFloat(),
                        availableScreenHeight - effectiveHeightPx + topInsetPx.toFloat() + bottomInsetPx.toFloat(),
                    )

                    // Update offsets to stay within bounds
                    offsetX = draggedX - baseX
                    offsetY = draggedY - baseY

                    // Debug position when it changes significantly
                    if (kotlin.math.abs(draggedX - lastLoggedX) > 30f ||
                        kotlin.math.abs(
                            draggedY - lastLoggedY,
                        ) > 30f
                    ) {
                        val maxX = availableScreenWidth - effectiveWidthPx
                        val maxY = availableScreenHeight - effectiveHeightPx
                        DebugLogger.info(
                            "VideoPreviewDialog",
                            "Video position: (${draggedX.toInt()}, ${draggedY.toInt()}) pixels from safe area top-left",
                        )
                        DebugLogger.info(
                            "VideoPreviewDialog",
                            "Safe area bounds: X=0-${maxX.toInt()}px, Y=0-${maxY.toInt()}px",
                        )
                        DebugLogger.info(
                            "VideoPreviewDialog",
                            "Bottom gap: ${(availableScreenHeight - draggedY - effectiveHeightPx).toInt()}px",
                        )
                        lastLoggedX = draggedX
                        lastLoggedY = draggedY
                    }

                    IntOffset(draggedX.toInt(), draggedY.toInt())
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
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
            val displayModifier = Modifier.size(
                width = (displayWidthPx / density.density).dp,
                height = (displayHeightPx / density.density).dp,
            )

            Box(
                modifier = displayModifier
                    .graphicsLayer(clip = false)
                    .background(Color.Black)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme3.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp),
                    ),
            ) {
                // Video player
                Box(
                    modifier = displayModifier
                        .graphicsLayer(clip = false)
                        .background(Color.Black)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme3.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp),
                        ),
                ) {
                    val (state, videoSize) = videoPlayer(
                        mediaItem = mediaItem,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(clip = false),
                        onStateChanged = { s ->
                            videoState = s
                        },
                        onEndReached = { player ->
                            // Loop the video
                            player.seekTo(0)
                            player.play()
                        },
                        rotationDegrees = 0,
                    )
                    videoState = state
                    videoSizeState = videoSize
                }

                // Click to toggle controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            .background(MaterialTheme3.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mediaItem.fileName,
                            style = MaterialTheme3.typography.titleMedium,
                            color = MaterialTheme3.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = { rotation = 0f },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = MaterialTheme3.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme3.colorScheme.onSurface,
                            )
                        }
                    }
                }

                // Bottom controls
                if (controlsVisible) {
                    VideoControls(
                        isPlaying = videoState.isPlaying,
                        position = videoState.position,
                        duration = videoState.duration,
                        onPlayPause = {
                            videoState.exoPlayer?.let { player ->
                                if (videoState.isPlaying) {
                                    player.pause()
                                } else {
                                    player.play()
                                }
                            }
                        },
                        onRewind = {
                            videoState.exoPlayer?.let { player ->
                                val newPos = (player.currentPosition - 10000).coerceAtLeast(0)
                                player.seekTo(newPos)
                            }
                        },
                        onForward = {
                            videoState.exoPlayer?.let { player ->
                                val newPos =
                                    (player.currentPosition + 10000).coerceAtMost(player.duration)
                                player.seekTo(newPos)
                            }
                        },
                        onSeek = { position ->
                            videoState.exoPlayer?.let { player ->
                                val seekPos = (position * player.duration).toLong()
                                player.seekTo(seekPos)
                            }
                        },
                        onFullscreen = {
                            isFullscreen = !isFullscreen
                            // TODO: Implement fullscreen mode
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
