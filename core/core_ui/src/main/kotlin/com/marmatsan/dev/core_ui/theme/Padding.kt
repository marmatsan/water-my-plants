package com.marmatsan.dev.core_ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class Padding(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val semiLarge: Dp = 24.dp,
    val large: Dp = 32.dp,
    val extraLarge: Dp = 64.dp
)

val LocalPadding = compositionLocalOf { Padding() }

/** Retrieves the current [Padding] at the call site's position in the hierarchy. */
val padding: Padding
    @Composable
    @ReadOnlyComposable
    get() = LocalPadding.current