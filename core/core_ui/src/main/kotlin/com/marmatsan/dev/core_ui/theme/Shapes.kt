package com.marmatsan.dev.core_ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object ShapesDefaults {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 28.dp
}

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(
        topStart = ShapesDefaults.extraSmall,
        topEnd = ShapesDefaults.extraSmall,
        bottomStart = ShapesDefaults.extraSmall,
        bottomEnd = ShapesDefaults.extraSmall
    ),
    small = RoundedCornerShape(
        topStart = ShapesDefaults.small,
        topEnd = ShapesDefaults.small,
        bottomStart = ShapesDefaults.small,
        bottomEnd = ShapesDefaults.small
    ),
    medium = RoundedCornerShape(
        topStart = ShapesDefaults.medium,
        topEnd = ShapesDefaults.medium,
        bottomStart = ShapesDefaults.medium,
        bottomEnd = ShapesDefaults.medium
    ),
    large = RoundedCornerShape(
        topStart = ShapesDefaults.large,
        topEnd = ShapesDefaults.large,
        bottomStart = ShapesDefaults.large,
        bottomEnd = ShapesDefaults.large
    ),
    extraLarge = RoundedCornerShape(
        topStart = ShapesDefaults.extraLarge,
        topEnd = ShapesDefaults.extraLarge,
        bottomStart = ShapesDefaults.extraLarge,
        bottomEnd = ShapesDefaults.extraLarge
    )
)