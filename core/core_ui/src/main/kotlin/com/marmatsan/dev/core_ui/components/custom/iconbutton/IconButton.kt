package com.marmatsan.dev.core_ui.components.custom.iconbutton

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(all = LocalSpacing.current.default),
    enabled : Boolean = true,
    iconButtonStyle: IconButtonStyle = IconButtonStyle.Standard,
    containerColor: Color = when (iconButtonStyle) {
        IconButtonStyle.Standard -> Color.Transparent
        IconButtonStyle.Filled -> MaterialTheme.colorScheme.primary
        IconButtonStyle.Tonal -> MaterialTheme.colorScheme.secondaryContainer
        IconButtonStyle.Outlined -> Color.Transparent
    },
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit = {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Filled.Settings,
            contentDescription = ""
        )
    }
) {

    val iconModifier = modifier.padding(padding)

    when (iconButtonStyle) {
        IconButtonStyle.Standard ->
            IconButton(
                modifier = iconModifier,
                enabled = enabled,
                onClick = { onClick() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
                content = icon
            )


        IconButtonStyle.Filled ->
            FilledIconButton(
                modifier = iconModifier,
                enabled = enabled,
                onClick = { onClick() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
                content = icon
            )


        IconButtonStyle.Tonal ->
            FilledTonalIconButton(
                modifier = iconModifier,
                enabled = enabled,
                colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
                onClick = { onClick() },
                content = icon
            )


        IconButtonStyle.Outlined ->
            OutlinedIconButton(
                modifier = iconModifier,
                enabled = enabled,
                colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
                onClick = { onClick() },
                content = icon
            )

    }
}

class IconButtonStyleParameterProvider : PreviewParameterProvider<IconButtonStyle> {
    override val values: Sequence<IconButtonStyle>
        get() = sequenceOf(*IconButtonStyle.entries.toTypedArray())
}

@Preview
@Composable
fun RelayIconButtonPreview(
    @PreviewParameter(IconButtonStyleParameterProvider::class) iconButtonStyle: IconButtonStyle
) {
    WaterMyPlantsTheme {
        IconButton(
            iconButtonStyle = iconButtonStyle,
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.Settings,
                    tint = when (iconButtonStyle) {
                        IconButtonStyle.Standard -> MaterialTheme.colorScheme.onSurfaceVariant
                        IconButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
                        IconButtonStyle.Tonal -> MaterialTheme.colorScheme.onSecondaryContainer
                        IconButtonStyle.Outlined -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    contentDescription = ""
                )
            }
        )
    }
}
