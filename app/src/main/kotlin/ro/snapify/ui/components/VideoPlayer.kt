package ro.snapify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import ro.snapify.data.entity.MediaItem
import androidx.media3.common.MediaItem as ExoMediaItem

data class VideoPlayerState(
    var isPlaying: Boolean = true,
    var position: Float = 0f,
    var duration: Long = 0L,
    var exoPlayer: ExoPlayer? = null
)

@androidx.media3.common.util.UnstableApi
@Composable
fun videoPlayer(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    onStateChanged: (VideoPlayerState) -> Unit = {},
    onEndReached: (ExoPlayer) -> Unit = {},
    resizeMode: Int = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
    rotationDegrees: Int = 0
): Pair<VideoPlayerState, androidx.compose.ui.unit.IntSize?> {
    val lifecycleOwner = LocalLifecycleOwner.current
    var videoState by remember { mutableStateOf(VideoPlayerState()) }
    var videoSize by remember { mutableStateOf<androidx.compose.ui.unit.IntSize?>(null) }

    // Pause video when app loses focus
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                videoState.exoPlayer?.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                // Set window to draw behind display cutouts
                val activity = ctx as? android.app.Activity
                activity?.window?.attributes?.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                activity?.window?.attributes = activity?.window?.attributes

                val exoPlayer = ExoPlayer.Builder(ctx).build()

                // Set URI
                val uri = "file://${mediaItem.filePath}".toUri()
                val exoMediaItem = ExoMediaItem.fromUri(uri)
                exoPlayer.setMediaItem(exoMediaItem)

                // Add listener for end reached and video size
                exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                            onEndReached(exoPlayer)
                        }
                    }

                    override fun onVideoSizeChanged(videoSizeData: androidx.media3.common.VideoSize) {
                        videoSize = androidx.compose.ui.unit.IntSize(
                            videoSizeData.width,
                            videoSizeData.height
                        )
                    }
                })

                // Prepare and play
                exoPlayer.prepare()
                exoPlayer.play()

                // Set resize mode
                setResizeMode(resizeMode)

                // Disable built-in controls
                useController = false

                player = exoPlayer

                videoState = videoState.copy(exoPlayer = exoPlayer)
            }
        },
        update = { _ ->
            // Update if needed
        },
        onRelease = { view ->
            // Clean up ExoPlayer
            view.player?.release()
        },
        modifier = modifier
    )

    // Poll position and duration
    LaunchedEffect(videoState.exoPlayer) {
        videoState.exoPlayer?.let { player ->
            while (true) {
                delay(1000) // Update every second
                videoState = videoState.copy(
                    position = player.currentPosition.toFloat() / player.duration.coerceAtLeast(1)
                        .toFloat(),
                    duration = player.duration
                )
                onStateChanged(videoState)
            }
        }
    }



    return Pair(videoState, videoSize)
}


