package ro.snapify.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.min

/**
 * Composable function that displays the overlay UI for screenshot management.
 * Provides buttons for quick deletion times, custom time picker, and keep option.
 */
@Composable
fun ScreenshotDetectionOverlay(
    detectedImage: ImageBitmap?,
    on15Minutes: () -> Unit,
    on2Hours: () -> Unit,
    on3Days: () -> Unit,
    on1Week: () -> Unit,
    onKeep: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit,
    onDismiss: () -> Unit,
    onCustomTime: (Int) -> Unit
) {
    val alphaAnimatable = remember { Animatable(0f) }
    val translationYAnimatable = remember { Animatable(100f) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        alphaAnimatable.animateTo(1f, animationSpec = tween(300))
        translationYAnimatable.animateTo(0f, animationSpec = tween(300))
    }

    val isOLED = MaterialTheme.colorScheme.surface == Color.Black
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.7f)
            .padding(8.dp)
            .alpha(alphaAnimatable.value)
            .graphicsLayer(translationY = translationYAnimatable.value)
            .then(
                if (isOLED) Modifier.border(
                    1.dp,
                    Color.White,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Screenshot Detect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

            Text(
            text = "Select a time when the screenshot should be deleted otherwise you can keep",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))

            // Row: Left - Detected Media, Right - Choices
            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Left: Detected Media
            Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
            ) {
            if (detectedImage != null) {
                Image(
                    bitmap = detectedImage,
                    contentDescription = "Detected Screenshot",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("No Image Detected")
            }
            }

            // Right: Choices
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    // Row 1: 1 Week | 3 Days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                                onClick = on1Week,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "1 Week")
                        }
                        Button(
                            onClick = on3Days,
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "3 Days")
                        }
                    }

                    // Row 2: 2 Hours | 15 Min
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    Button(
                    onClick = on2Hours,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                        contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "2 Hours")
                    }
                    Button(
                    onClick = on15Minutes,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                        contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "15 Min")
                        }
                    }

                    // Custom Time Picker
                    CustomTimePicker(
                        onTimeSelected = onCustomTime,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Keep Button
                    Button(
                        onClick = onKeep,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Keep")
                    }

                    // Share and Delete Button
                    Button(
                        onClick = onShare,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Share and Delete")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ScreenshotDetectionOverlayPreview() {
    ScreenshotDetectionOverlay(
        detectedImage = null,
        on15Minutes = {},
        on2Hours = {},
        on3Days = {},
        on1Week = {},
        onKeep = {},
        onShare = {},
        onClose = {},
        onDismiss = {},
        onCustomTime = {}
    )
}

@Composable
private fun CustomTimePicker(
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var minutes by remember { mutableIntStateOf(30) }
    var scrollTrigger by remember { mutableIntStateOf(0) }
    var initialY by remember { mutableFloatStateOf(0f) }
    var adjustmentMode by remember { mutableIntStateOf(0) } // 0 none, 1 increase, -1 decrease
    var currentDistance by remember { mutableFloatStateOf(0f) }
    var modeStartTime by remember { mutableStateOf(0L) }
    val textScale = remember { Animatable(1f) }
    val gearRotation = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(scrollTrigger) {
// Animate text scale in/out
        textScale.animateTo(1.2f, tween(150))
        textScale.animateTo(1f, tween(150))
// Animate gear spin clockwise
        gearRotation.animateTo(gearRotation.value + 360f, tween(400))
    }

    androidx.compose.runtime.LaunchedEffect(adjustmentMode) {
        while (adjustmentMode != 0) {
            val elapsed = System.currentTimeMillis() - modeStartTime
            val baseSpeed = min(1f + (elapsed / 1000f) * 1f + (currentDistance / 100f) * 1f, 50f)
            val dayMultiplier = if (minutes >= 1440) 5f else 1f
            val speedFactor = min(baseSpeed * dayMultiplier, 200f)
            val increment = min(speedFactor.toInt() / 5, 60).coerceAtLeast(1)
            if (adjustmentMode == 1) {
                minutes = (minutes + increment).coerceAtMost(Int.MAX_VALUE)
                scrollTrigger++
            } else if (adjustmentMode == -1) {
                minutes = (minutes - increment).coerceAtLeast(1)
                scrollTrigger++
            }
            val delay = (200L / speedFactor).toLong().coerceAtLeast(5L)
            kotlinx.coroutines.delay(delay)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            initialY = offset.y
                            adjustmentMode = 0
                            currentDistance = 0f
                        },
                        onDragEnd = { adjustmentMode = 0 },
                        onDragCancel = { adjustmentMode = 0 }
                    ) { change, _ ->
                        val currentY = change.position.y
                        val newDistance = kotlin.math.abs(currentY - initialY)
                        currentDistance = newDistance
                        val newMode =
                            if (currentY < initialY) 1 else if (currentY > initialY) -1 else 0
                        if (adjustmentMode != newMode) {
                            modeStartTime = System.currentTimeMillis()
                            adjustmentMode = newMode
                        }
                        change.consume()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Wheel",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = gearRotation.value),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                    text = formatTime(minutes),
                    modifier = Modifier
                        .clickable { onTimeSelected(minutes) }
                        .graphicsLayer(scaleX = textScale.value, scaleY = textScale.value),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun CustomTimePickerPreview() {
    CustomTimePicker(onTimeSelected = {})
}

private fun formatTime(minutes: Int): String {
    val parts = mutableListOf<String>()
    var remaining = minutes

    val years = remaining / 525600
    if (years > 0) {
        parts.add("$years year${if (years > 1) "s" else ""}")
        remaining %= 525600
    }

    val weeks = remaining / 10080
    if (weeks > 0) {
        parts.add("$weeks week${if (weeks > 1) "s" else ""}")
        remaining %= 10080
    }

    val days = remaining / 1440
    if (days > 0) {
        parts.add("$days day${if (days > 1) "s" else ""}")
        remaining %= 1440
    }

    val hours = remaining / 60
    if (hours > 0) {
        parts.add("$hours hour${if (hours > 1) "s" else ""}")
        remaining %= 60
    }

    if (remaining > 0 || parts.isEmpty()) {
        parts.add("$remaining minute${if (remaining != 1) "s" else ""}")
    }

    return parts.joinToString(" ")
}

