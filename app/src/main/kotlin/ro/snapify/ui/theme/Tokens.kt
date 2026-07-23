package ro.snapify.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ro.snapify.data.preferences.AppPreferences

const val MIN_CORNER_SCALE = 0.5f
const val MAX_CORNER_SCALE = 1.5f
const val DEFAULT_CORNER_SCALE = 1f

/** Full color role set for the Snapify design system. UI code must only consume these roles. */
@Immutable
data class SnapifyColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val hairline: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val scrim: Color,
    val onScrim: Color,
    val isDark: Boolean,
)

/** A user-selectable accent with a guaranteed-contrast companion color. */
@Immutable
data class SnapifyAccent(
    val key: String,
    val accent: Color,
    val onAccent: Color,
)

val SnapifyAccentCatalog = listOf(
    SnapifyAccent("amber", Color(0xFFE8B54A), Color(0xFF241A05)),
    SnapifyAccent("copper", Color(0xFFE0855C), Color(0xFF2A1208)),
    SnapifyAccent("sage", Color(0xFFA3C98B), Color(0xFF12230C)),
    SnapifyAccent("sky", Color(0xFF8FB8DC), Color(0xFF0E2030)),
    SnapifyAccent("lilac", Color(0xFFC4A9E8), Color(0xFF21123A)),
    SnapifyAccent("rose", Color(0xFFE69DB0), Color(0xFF34101C)),
)

val DefaultSnapifyAccent = SnapifyAccentCatalog.first()

fun snapifyAccentFor(argb: Long?): SnapifyAccent =
    SnapifyAccentCatalog.firstOrNull { it.accent.value.toLong() == argb } ?: DefaultSnapifyAccent

/** Rounded-forward shape scale. All values derive from the user corner scale. */
@Immutable
data class SnapifyShapes(
    val card: Dp,
    val sheet: Dp,
    val dialog: Dp,
    val button: Dp,
    val tile: Dp,
    val field: Dp,
    val thumbnail: Dp,
) {
    val cardShape: Shape get() = RoundedCornerShape(card)
    val sheetShape: Shape get() = RoundedCornerShape(sheet)
    val dialogShape: Shape get() = RoundedCornerShape(dialog)
    val buttonShape: Shape get() = RoundedCornerShape(button)
    val tileShape: Shape get() = RoundedCornerShape(tile)
    val fieldShape: Shape get() = RoundedCornerShape(this.field)
    val thumbnailShape: Shape get() = RoundedCornerShape(thumbnail)
    val pillShape: Shape get() = CircleShape
}

fun snapifyShapes(cornerScale: Float): SnapifyShapes {
    val scale = cornerScale.coerceIn(MIN_CORNER_SCALE, MAX_CORNER_SCALE)
    return SnapifyShapes(
        card = 28.dp * scale,
        sheet = 32.dp * scale,
        dialog = 36.dp * scale,
        button = 22.dp * scale,
        tile = 24.dp * scale,
        field = 18.dp * scale,
        thumbnail = 20.dp * scale,
    )
}

@Immutable
data class SnapifySpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

@Immutable
data class SnapifyTokens(
    val colors: SnapifyColors,
    val shapes: SnapifyShapes,
    val spacing: SnapifySpacing,
)

private fun baseColors(dark: Boolean, oled: Boolean): SnapifyColors = if (dark) {
    SnapifyColors(
        background = if (oled) Color(0xFF000000) else Color(0xFF0F0D0B),
        surface = if (oled) Color(0xFF0C0A08) else Color(0xFF17140F),
        surfaceRaised = if (oled) Color(0xFF161310) else Color(0xFF211C15),
        ink = Color(0xFFF3EDE0),
        inkSoft = Color(0xFFBCB2A2),
        inkFaint = Color(0xFF7F7666),
        accent = DefaultSnapifyAccent.accent,
        onAccent = DefaultSnapifyAccent.onAccent,
        accentSoft = DefaultSnapifyAccent.accent.copy(alpha = 0.16f),
        onAccentSoft = DefaultSnapifyAccent.accent,
        hairline = if (oled) Color(0xFF26201A) else Color(0xFF2D2721),
        success = Color(0xFFA3C98B),
        warning = Color(0xFFE0B45C),
        danger = Color(0xFFE07A68),
        scrim = Color(0xFF000000),
        onScrim = Color(0xFFF3EDE0),
        isDark = true,
    )
} else {
    SnapifyColors(
        background = Color(0xFFF6F1E7),
        surface = Color(0xFFFDFAF3),
        surfaceRaised = Color(0xFFFFFFFF),
        ink = Color(0xFF201A12),
        inkSoft = Color(0xFF5E5545),
        inkFaint = Color(0xFF968C79),
        accent = DefaultSnapifyAccent.accent,
        onAccent = DefaultSnapifyAccent.onAccent,
        accentSoft = DefaultSnapifyAccent.accent.copy(alpha = 0.16f),
        onAccentSoft = DefaultSnapifyAccent.accent,
        hairline = Color(0xFFE5DCC9),
        success = Color(0xFF4E7D43),
        warning = Color(0xFF9A6E14),
        danger = Color(0xFFB44436),
        scrim = Color(0xFF000000),
        onScrim = Color(0xFFF3EDE0),
        isDark = false,
    )
}

/** User-persisted theme overrides. null accent means the preset default. */
@Immutable
data class ThemeCustomization(
    val accentArgb: Long? = null,
    val cornerScale: Float = DEFAULT_CORNER_SCALE,
)

fun snapifyTokens(
    dark: Boolean,
    oled: Boolean,
    customization: ThemeCustomization = ThemeCustomization(),
): SnapifyTokens {
    val accent = snapifyAccentFor(customization.accentArgb)
    val base = baseColors(dark, oled)
    return SnapifyTokens(
        colors = base.copy(
            accent = accent.accent,
            onAccent = accent.onAccent,
            accentSoft = accent.accent.copy(alpha = 0.16f),
            onAccentSoft = accent.accent,
        ),
        shapes = snapifyShapes(customization.cornerScale),
        spacing = SnapifySpacing(),
    )
}

val LocalSnapifyTokens = staticCompositionLocalOf { snapifyTokens(dark = true, oled = false) }

/** Entry point for all UI code: colors, shapes and spacing, never literals. */
object SnapifyTheme {
    val colors: SnapifyColors
        @Composable @ReadOnlyComposable get() = LocalSnapifyTokens.current.colors
    val shapes: SnapifyShapes
        @Composable @ReadOnlyComposable get() = LocalSnapifyTokens.current.shapes
    val spacing: SnapifySpacing
        @Composable @ReadOnlyComposable get() = LocalSnapifyTokens.current.spacing
}

/**
 * Reads persisted customization through the process-global DataStore.
 * Safe in any Compose context, including service windows without a ViewModelStore.
 */
@Composable
fun rememberThemeCustomization(): ThemeCustomization {
    val context = LocalContext.current.applicationContext
    val preferences = remember { AppPreferences(context) }
    val accentArgb by preferences.themeAccent.collectAsState(initial = null)
    val cornerScale by preferences.themeCornerScale.collectAsState(initial = DEFAULT_CORNER_SCALE)
    return ThemeCustomization(accentArgb = accentArgb, cornerScale = cornerScale)
}

internal fun SnapifyTokens.toColorScheme() = if (colors.isDark) {
    darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        primaryContainer = colors.accentSoft,
        onPrimaryContainer = colors.onAccentSoft,
        secondary = colors.inkSoft,
        onSecondary = colors.background,
        secondaryContainer = colors.surfaceRaised,
        onSecondaryContainer = colors.ink,
        tertiary = colors.warning,
        onTertiary = colors.background,
        tertiaryContainer = colors.surfaceRaised,
        onTertiaryContainer = colors.inkSoft,
        error = colors.danger,
        onError = colors.background,
        errorContainer = colors.danger.copy(alpha = 0.16f),
        onErrorContainer = colors.danger,
        background = colors.background,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceRaised,
        onSurfaceVariant = colors.inkSoft,
        outline = colors.hairline,
        outlineVariant = colors.hairline.copy(alpha = 0.6f),
        inversePrimary = colors.accent,
        surfaceTint = colors.accent,
        scrim = colors.scrim,
    )
} else {
    lightColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        primaryContainer = colors.accentSoft,
        onPrimaryContainer = colors.onAccentSoft,
        secondary = colors.inkSoft,
        onSecondary = colors.surface,
        secondaryContainer = colors.surfaceRaised,
        onSecondaryContainer = colors.ink,
        tertiary = colors.warning,
        onTertiary = colors.surface,
        tertiaryContainer = colors.surfaceRaised,
        onTertiaryContainer = colors.inkSoft,
        error = colors.danger,
        onError = colors.surface,
        errorContainer = colors.danger.copy(alpha = 0.12f),
        onErrorContainer = colors.danger,
        background = colors.background,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceRaised,
        onSurfaceVariant = colors.inkSoft,
        outline = colors.hairline,
        outlineVariant = colors.hairline.copy(alpha = 0.7f),
        inversePrimary = colors.accent,
        surfaceTint = colors.accent,
        scrim = colors.scrim,
    )
}

