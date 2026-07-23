package ro.snapify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    DYNAMIC,
    OLED,
}

/**
 * Applies the Snapify token system. Every surface in the app derives from
 * [SnapifyTokens] provided here; customization flows in as a parameter so
 * activity call sites avoid a second async read, while service windows fall
 * back to [rememberThemeCustomization].
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    customization: ThemeCustomization? = null,
    dynamicColor: Boolean = false,
    skipWindowSetup: Boolean = false,
    content: @Composable () -> Unit,
) {
    val resolvedCustomization = customization ?: rememberThemeCustomization()
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.OLED -> true
        ThemeMode.SYSTEM, ThemeMode.DYNAMIC -> systemDark
    }
    val oled = themeMode == ThemeMode.OLED
    val context = LocalContext.current

    val tokens = remember(dark, oled, resolvedCustomization) {
        snapifyTokens(dark, oled, resolvedCustomization)
    }

    val colorScheme = if (
        themeMode == ThemeMode.DYNAMIC && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        tokens.toColorScheme()
    }

    if (!skipWindowSetup) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                // Ensure window background matches theme for edge-to-edge consistency
                window.setBackgroundDrawable(colorScheme.background.toArgb().toDrawable())
            }
        }
    }

    CompositionLocalProvider(LocalSnapifyTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
