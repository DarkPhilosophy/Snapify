package ro.snapify.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun DuoDrawer(
    isOpen: Boolean,
    onOpenDrawer: () -> Unit,
    onCloseDrawer: () -> Unit,
    menuContent: @Composable (Boolean) -> Unit,
    content: @Composable () -> Unit,
    dialogContent: @Composable () -> Unit = {},
    showDialog: Boolean = false,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    // Large offset to ensure drawer is off-screen when closed
    val offScreenOffset = -1000.dp

    // Enhanced animations with staggered easing
    val drawerOffset by animateDpAsState(
        targetValue = when {
            !isOpen -> offScreenOffset
            isOpen && !showDialog -> 0.dp
            isOpen && showDialog -> offScreenOffset / 2
            else -> offScreenOffset
        },
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "drawerOffset",
    )

    // Smooth overlay fade
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.5f else 0f,
        animationSpec = tween(durationMillis = 400, easing = EaseOutCubic),
        label = "overlayAlpha",
    )

    // Theming: OLED support
    val isOLED = MaterialTheme.colorScheme.surface == Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isOpen) "Drawer open" else "Drawer closed" },
    ) {
        // Main content with accessibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = !isOpen }, // Focus management
        ) {
            content()
        }

        // Semi-transparent overlay with fade animation and haptic feedback
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 50f && isOpen) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCloseDrawer()
                            }
                        }
                    },
            )
        }

        // Drawer sliding from left with drag-to-close
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = drawerOffset)
                .background(if (isOLED) Color.Black else MaterialTheme.colorScheme.surface)
                .padding(top = 0.dp) // Draw over status bar and camera notch
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 50f && isOpen) {
                            onCloseDrawer()
                        }
                    }
                }
                .semantics { contentDescription = "Settings menu drawer" },
        ) {
            menuContent(isOpen)
        }
    }

    // Dialog with focus management
    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = true }
                .onFocusChanged { if (!it.isFocused) onCloseDrawer() }, // Close on focus loss
        ) {
            dialogContent()
        }
    } else {
        dialogContent()
    }
}
