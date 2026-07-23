package ro.snapify.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ro.snapify.ui.theme.SnapifyTheme

private const val PANEL_MAX_WIDTH_DP = 380
private const val PANEL_WIDTH_FRACTION = 0.82f
private const val CONTENT_PARALLAX = 1f
private const val CONTENT_SCALE_DROP = 0.06f
private const val VELOCITY_THRESHOLD_PX = 700f

/** Progress-driven state for [StageDrawer]: 0f = closed, 1f = open. */
@Stable
class StageDrawerState internal constructor(
    internal val progress: Animatable<Float, *>,
    private val scope: CoroutineScope,
) {
    val isOpen: Boolean get() = progress.value > 0.5f

    fun open() {
        scope.launch { progress.animateTo(1f, settleSpring()) }
    }

    fun close() {
        scope.launch { progress.animateTo(0f, settleSpring()) }
    }

    fun toggle() {
        if (isOpen) close() else open()
    }
}

private fun settleSpring() = spring<Float>(
    dampingRatio = 0.82f,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun rememberStageDrawerState(): StageDrawerState {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    return remember { StageDrawerState(progress, scope) }
}

/**
 * A gesture-driven side panel that is part of the layout, not an overlay:
 * the main content recedes (parallax + scale) as the panel slides in, and a
 * drag handle rides the panel's trailing edge so the button and the panel
 * move as one physical object.
 */
@Composable
fun StageDrawer(
    state: StageDrawerState,
    menuContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tokens = SnapifyTheme.colors
    val cardRadius = SnapifyTheme.shapes.card

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val panelWidthPx = with(density) {
            minOf(
                constraints.maxWidth.toFloat() * PANEL_WIDTH_FRACTION,
                PANEL_MAX_WIDTH_DP.dp.toPx(),
            )
        }

        fun settleTarget(velocityX: Float): Float = when {
            velocityX > VELOCITY_THRESHOLD_PX -> 1f
            velocityX < -VELOCITY_THRESHOLD_PX -> 0f
            state.progress.value > 0.5f -> 1f
            else -> 0f
        }

        fun Modifier.stageDrag(): Modifier = this.pointerInput(panelWidthPx) {
            // Manual velocity estimation: VelocityTracker throws on non-monotonic
            // event times, which happens on some devices during fast close-drags.
            val samples = ArrayDeque<Pair<Long, Float>>(8)
            detectHorizontalDragGestures(
                onDragStart = {
                    samples.clear()
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    if (samples.lastOrNull()?.first != change.uptimeMillis) {
                        samples.addLast(change.uptimeMillis to change.position.x)
                        while (samples.size > 8) samples.removeFirst()
                    }
                    scope.launch {
                        state.progress.snapTo(
                            (state.progress.value + dragAmount / panelWidthPx).coerceIn(0f, 1f),
                        )
                    }
                },
                onDragEnd = {
                    val velocity = if (samples.size >= 2) {
                        val (t0, x0) = samples.first()
                        val (t1, x1) = samples.last()
                        if (t1 > t0) (x1 - x0) / (t1 - t0) * 1000f else 0f
                    } else {
                        0f
                    }
                    scope.launch {
                        state.progress.animateTo(settleTarget(velocity), settleSpring())
                    }
                },
                onDragCancel = {
                    scope.launch {
                        state.progress.animateTo(settleTarget(0f), settleSpring())
                    }
                },
            )
        }

        // Main content: recedes and scales down as the panel takes the stage.
        // It never intercepts horizontal drags - the grid/list owns those gestures.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Spring overshoot can push progress slightly below 0 or above 1;
                    // corner radii and alpha must stay within range.
                    val p = state.progress.value.coerceIn(0f, 1f)
                    translationX = panelWidthPx * p * CONTENT_PARALLAX
                    val scale = 1f - CONTENT_SCALE_DROP * p
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = if (p > 0.01f) 8.dp.toPx() * p else 0f
                    clip = p > 0.01f
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        cardRadius * p,
                    )
                },
        ) {
            content()

            // Always composed; alpha is applied in the graphics layer so progress
            // changes never trigger recomposition. Hit-testing activates only when
            // the panel is (nearly) open, so taps fall through when closed.
            val scrimActive by remember {
                androidx.compose.runtime.derivedStateOf { state.progress.value > 0.01f }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = state.progress.value.coerceIn(0f, 1f) }
                    .background(tokens.scrim.copy(alpha = 0.12f))
                    .then(
                        if (scrimActive) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (state.progress.value > 0.5f) state.close()
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
            )
        }

        // Dedicated left-edge strip for drag-to-open; invisible and narrow so it
        // does not fight the content's own gestures.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(20.dp)
                .stageDrag(),
        )

        // The panel itself: measured offset, never a hardcoded off-screen guess.
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { panelWidthPx.toDp() })
                .offset {
                    IntOffset((-panelWidthPx * (1f - state.progress.value)).toInt(), 0)
                }
                .graphicsLayer {
                    // Menu slides slightly faster than its own frame for depth.
                    clip = true
                }
                .stageDrag(),
            color = tokens.surface,
            tonalElevation = 2.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset((-panelWidthPx * 0.12f * (1f - state.progress.value)).toInt(), 0)
                    },
            ) {
                menuContent()
            }
        }
        // Traveling menu button: its own entity, NOT part of the panel. The
        // panel slides OVER its parked spot, so the button emerges from behind
        // the drawer and lands at the panel's inner trailing edge.
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    val closedX = with(density) { 16.dp.toPx() }
                    val openX = panelWidthPx - with(density) { (44 + 16).dp.toPx() }
                    IntOffset(
                        (closedX + (openX - closedX) * state.progress.value).toInt(),
                        with(density) { 36.dp.toPx() }.toInt(),
                    )
                }
                .width(44.dp)
                .height(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { state.toggle() },
            shape = SnapifyTheme.shapes.buttonShape,
            color = tokens.surfaceRaised,
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.accent),
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Crossfade(targetState = state.isOpen, label = "menuButtonIcon") { open ->
                    if (open) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close menu",
                            tint = tokens.accent,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open menu",
                            tint = tokens.accent,
                        )
                    }
                }
            }
        }

    }
}
