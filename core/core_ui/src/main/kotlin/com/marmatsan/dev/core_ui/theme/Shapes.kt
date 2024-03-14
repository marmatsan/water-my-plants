package com.marmatsan.dev.core_ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object ShapeDefaults {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 28.dp
}

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(
        topStart = ShapeDefaults.extraSmall,
        topEnd = ShapeDefaults.extraSmall,
        bottomStart = ShapeDefaults.extraSmall,
        bottomEnd = ShapeDefaults.extraSmall
    ),
    small = RoundedCornerShape(
        topStart = ShapeDefaults.small,
        topEnd = ShapeDefaults.small,
        bottomStart = ShapeDefaults.small,
        bottomEnd = ShapeDefaults.small
    ),
    medium = RoundedCornerShape(
        topStart = ShapeDefaults.medium,
        topEnd = ShapeDefaults.medium,
        bottomStart = ShapeDefaults.medium,
        bottomEnd = ShapeDefaults.medium
    ),
    large = RoundedCornerShape(
        topStart = ShapeDefaults.large,
        topEnd = ShapeDefaults.large,
        bottomStart = ShapeDefaults.large,
        bottomEnd = ShapeDefaults.large
    ),
    extraLarge = RoundedCornerShape(
        topStart = ShapeDefaults.extraLarge,
        topEnd = ShapeDefaults.extraLarge,
        bottomStart = ShapeDefaults.extraLarge,
        bottomEnd = ShapeDefaults.extraLarge
    )
)