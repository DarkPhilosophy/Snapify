package ro.snapify.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@Composable
fun DuoDrawer(
    isOpen: Boolean,
    onOpenDrawer: () -> Unit,
    onCloseDrawer: () -> Unit,
    menuContent: @Composable (Boolean) -> Unit,
    content: @Composable () -> Unit,
    dialogContent: @Composable () -> Unit = {},
    showDialog: Boolean = false
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = density.run { windowInfo.containerSize.width.toDp() }

    val drawerOffset by animateDpAsState(
        targetValue = when {
            !isOpen -> -screenWidth
            isOpen && !showDialog -> 0.dp
            isOpen && showDialog -> screenWidth / 2
            else -> -screenWidth
        },
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        content()

        // Semi-transparent overlay when drawer is open
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        // Drawer sliding from left
        Box(
            modifier = Modifier
                .fillMaxSize() // Full width drawer
                .offset(x = drawerOffset)
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 0.dp) // Draw over status bar and camera notch
        ) {
            menuContent(isOpen)
        }
    }

    dialogContent()
}
