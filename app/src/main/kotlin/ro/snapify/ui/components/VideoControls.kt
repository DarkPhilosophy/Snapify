package ro.snapify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ro.snapify.util.TimeUtils

@Composable
fun VideoControls(
    isPlaying: Boolean,
    position: Float,
    duration: Long,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Float) -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Seek bar
        Slider(
            value = position,
            onValueChange = onSeek,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(), // Full width
        )

        // Buttons row with time between Forward and Rotate
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Play/Pause
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                )
            }

            // Rewind
            IconButton(
                onClick = onRewind,
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind",
                    tint = Color.White,
                )
            }

            // Time display between Rewind and Forward
            val currentTime = (position * duration).toLong()
            val totalTime = duration
            Text(
                text = "${TimeUtils.formatTime(currentTime / 1000)} / ${
                    TimeUtils.formatTime(
                        totalTime / 1000,
                    )
                }",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
            )

            // Forward
            IconButton(
                onClick = onForward,
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Forward",
                    tint = Color.White,
                )
            }

            // TODO: Implement video rotation - current implementation using graphicsLayer doesn't properly rotate video content
            // Rotate button hidden until proper ExoPlayer video rotation is implemented
            // IconButton(
            //     onClick = onRotate,
            //     modifier = Modifier
            //         .width(32.dp)
            //         .height(32.dp)
            // ) {
            //     Icon(
            //         imageVector = Icons.AutoMirrored.Filled.RotateRight,
            //         contentDescription = "Rotate",
            //         tint = Color.White
            //     )
            // }

            // Fullscreen button
            IconButton(
                onClick = onFullscreen,
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                )
            }
        }
    }
}
