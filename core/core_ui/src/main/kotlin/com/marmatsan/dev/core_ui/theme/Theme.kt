package com.marmatsan.dev.core_ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.marmatsan.dev.core_ui.dimensions.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = primary80,
    onPrimary = primary20,
    primaryContainer = primary30,
    onPrimaryContainer = primary90,
    inversePrimary = primary40,
    secondary = secondary80,
    onSecondary = secondary20,
    secondaryContainer = secondary30,
    onSecondaryContainer = secondary90,
    tertiary = tertiary80,
    onTertiary = tertiary20,
    tertiaryContainer = tertiary30,
    onTertiaryContainer = tertiary90,
    background = neutral6,
    onBackground = neutral90,
    surface = neutral6,
    onSurface = neutral90,
    surfaceVariant = neutralVariant30,
    onSurfaceVariant = neutralVariant80,
    surfaceTint = primary80,
    inverseSurface = neutral90,
    inverseOnSurface = neutral20,
    error = error80,
    onError = error20,
    errorContainer = error30,
    onErrorContainer = error90,
    outline = neutralVariant60,
    outlineVariant = neutralVariant30,
    scrim = neutral0,
    surfaceBright = neutral24,
    surfaceContainer = neutral12,
    surfaceContainerHigh = neutral17,
    surfaceContainerHighest = neutral22,
    surfaceContainerLow = neutral10,
    surfaceContainerLowest = neutral4,
    surfaceDim = neutral6
)

private val LightColorScheme = lightColorScheme(
    primary = primary40,
    onPrimary = primary100,
    primaryContainer = primary90,
    onPrimaryContainer = primary10,
    inversePrimary = primary80,
    secondary = secondary40,
    onSecondary = secondary100,
    secondaryContainer = secondary90,
    onSecondaryContainer = secondary10,
    tertiary = tertiary40,
    onTertiary = tertiary100,
    tertiaryContainer = tertiary90,
    onTertiaryContainer = tertiary10,
    background = neutral98,
    onBackground = neutral10,
    surface = neutral98,
    onSurface = neutral10,
    surfaceVariant = neutralVariant90,
    onSurfaceVariant = neutralVariant30,
    surfaceTint = primary40,
    inverseSurface = neutral20,
    inverseOnSurface = neutral95,
    error = error40,
    onError = error100,
    errorContainer = error90,
    onErrorContainer = error10,
    outline = neutralVariant50,
    outlineVariant = neutralVariant80,
    scrim = neutral0,
    surfaceBright = neutral98,
    surfaceContainer = neutral94,
    surfaceContainerHigh = neutral92,
    surfaceContainerHighest = neutral90,
    surfaceContainerLow = neutral96,
    surfaceContainerLowest = neutral100,
    surfaceDim = neutral87
)

// Extended MaterialTheme colors
val ColorScheme.onBackgroundVariant: Color
    @Composable
    get() = if (!isSystemInDarkTheme()) neutral40 else neutral70

@Composable
fun WaterMyPlantsTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color is available on Android 12+
    val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        supportsDynamicColors && isInDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        supportsDynamicColors && !isInDarkTheme -> dynamicLightColorScheme(LocalContext.current)
        isInDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // TODO: bugfix-Only change if app theme is device theme is set to dark
            // TODO: feature-Change to edge-to-edge https://developer.android.com/develop/ui/views/layout/edge-to-edge
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isInDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}