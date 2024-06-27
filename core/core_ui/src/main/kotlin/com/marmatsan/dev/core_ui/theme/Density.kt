package com.marmatsan.dev.core_ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val baseDensity: Dp = 4.dp

@Stable
data class Density(
    val negativeThree: Dp = -(4 * baseDensity.value).dp,
    val negativeTwo: Dp = -(3 * baseDensity.value).dp,
    val negativeOne: Dp = -(2 * baseDensity.value).dp,
    val none: Dp = 0.dp,
    val positiveOne: Dp = baseDensity,
    val positiveTwo: Dp = (2 * baseDensity.value).dp,
    val positiveThree: Dp = (3 * baseDensity.value).dp,
    val positiveFour: Dp = (4 * baseDensity.value).dp
)

val LocalDensity = compositionLocalOf { Density() }

/** Retrieves the current [Density] at the call site's position in the hierarchy. */
val density: Density
    @Composable
    @ReadOnlyComposable
    get() = LocalDensity.current