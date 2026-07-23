package ro.snapify.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Colored glow halo around an element, implemented with ambient and spot
 * shadows instead of a border - the light appears to come from the element
 * itself. Clip stays off so the halo can bleed past the element's bounds.
 */
fun Modifier.glow(
    color: Color,
    elevation: Dp = 20.dp,
    shape: Shape = CircleShape,
): Modifier = this.graphicsLayer {
    shadowElevation = elevation.toPx()
    this.shape = shape
    ambientShadowColor = color
    spotShadowColor = color
}
