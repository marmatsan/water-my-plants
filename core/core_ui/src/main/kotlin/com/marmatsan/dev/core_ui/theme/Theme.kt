package com.marmatsan.dev.core_ui.dimensions.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.marmatsan.dev.core_ui.theme.error10
import com.marmatsan.dev.core_ui.theme.error100
import com.marmatsan.dev.core_ui.theme.error20
import com.marmatsan.dev.core_ui.theme.error30
import com.marmatsan.dev.core_ui.theme.error40
import com.marmatsan.dev.core_ui.theme.error80
import com.marmatsan.dev.core_ui.theme.error90
import com.marmatsan.dev.core_ui.theme.neutral0
import com.marmatsan.dev.core_ui.theme.neutral10
import com.marmatsan.dev.core_ui.theme.neutral20
import com.marmatsan.dev.core_ui.theme.neutral40
import com.marmatsan.dev.core_ui.theme.neutral6
import com.marmatsan.dev.core_ui.theme.neutral70
import com.marmatsan.dev.core_ui.theme.neutral90
import com.marmatsan.dev.core_ui.theme.neutral95
import com.marmatsan.dev.core_ui.theme.neutral98
import com.marmatsan.dev.core_ui.theme.neutralVariant30
import com.marmatsan.dev.core_ui.theme.neutralVariant50
import com.marmatsan.dev.core_ui.theme.neutralVariant60
import com.marmatsan.dev.core_ui.theme.neutralVariant80
import com.marmatsan.dev.core_ui.theme.neutralVariant90
import com.marmatsan.dev.core_ui.theme.primary10
import com.marmatsan.dev.core_ui.theme.primary100
import com.marmatsan.dev.core_ui.theme.primary20
import com.marmatsan.dev.core_ui.theme.primary30
import com.marmatsan.dev.core_ui.theme.primary40
import com.marmatsan.dev.core_ui.theme.primary80
import com.marmatsan.dev.core_ui.theme.primary90
import com.marmatsan.dev.core_ui.theme.secondary10
import com.marmatsan.dev.core_ui.theme.secondary100
import com.marmatsan.dev.core_ui.theme.secondary20
import com.marmatsan.dev.core_ui.theme.secondary30
import com.marmatsan.dev.core_ui.theme.secondary40
import com.marmatsan.dev.core_ui.theme.secondary80
import com.marmatsan.dev.core_ui.theme.secondary90
import com.marmatsan.dev.core_ui.theme.shapes
import com.marmatsan.dev.core_ui.theme.tertiary10
import com.marmatsan.dev.core_ui.theme.tertiary100
import com.marmatsan.dev.core_ui.theme.tertiary20
import com.marmatsan.dev.core_ui.theme.tertiary30
import com.marmatsan.dev.core_ui.theme.tertiary40
import com.marmatsan.dev.core_ui.theme.tertiary80
import com.marmatsan.dev.core_ui.theme.tertiary90

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
    scrim = neutral0
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
    scrim = neutral0
)

// Extended MaterialTheme colors
val ColorScheme.onBackgroundVariant: Color
    @Composable
    get() = if (isSystemInDarkTheme()) neutral70 else neutral40

@Composable
fun WaterMyPlantsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color is available on Android 12+
    val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        supportsDynamicColors && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        supportsDynamicColors && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = Typography,
        content = content
    )
}