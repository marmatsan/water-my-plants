package com.marmatsan.dev.core_ui.components.relay

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.surfaceContainerLow

/*
@Composable
fun RelayButton(
    modifier: Modifier = Modifier,
    buttonStyle: ButtonStyle = ButtonStyle.Filled,
    buttonState: ButtonState = ButtonState.Enabled,
    containerColor: Color = when (buttonStyle) {
        ButtonStyle.Filled -> MaterialTheme.colorScheme.primary
        ButtonStyle.Outlined -> Color.Transparent
        ButtonStyle.Text -> Color.Transparent
        ButtonStyle.Elevated -> MaterialTheme.colorScheme.surfaceContainerLow
        ButtonStyle.Tonal -> MaterialTheme.colorScheme.secondaryContainer
    },
    label: String = String.Empty,
    labelColor: Color = when (buttonStyle) {
        ButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
        ButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
        ButtonStyle.Text -> MaterialTheme.colorScheme.primary
        ButtonStyle.Elevated -> MaterialTheme.colorScheme.primary
        ButtonStyle.Tonal -> MaterialTheme.colorScheme.onSecondaryContainer
    },
    icon: @Composable() (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    val buttonModifier = modifier
        .height(ButtonDefaults.MinHeight)

    when (buttonStyle) {
        ButtonStyle.Filled ->
            Button(
                modifier = buttonModifier,
                enabled = buttonState == ButtonState.Enabled,
                colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                onClick = { onClick() }
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelColor,
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Outlined ->
            OutlinedButton(
                modifier = buttonModifier,
                enabled = buttonState == ButtonState.Enabled,
                onClick = { onClick() }
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelColor,
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Text ->
            TextButton(
                modifier = buttonModifier,
                enabled = buttonState == ButtonState.Enabled,
                onClick = { onClick() }
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelColor,
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Elevated ->
            ElevatedButton(
                modifier = buttonModifier,
                enabled = buttonState == ButtonState.Enabled,
                onClick = { onClick() }
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelColor,
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }


        ButtonStyle.Tonal ->
            FilledTonalButton(
                modifier = buttonModifier,
                enabled = buttonState == ButtonState.Enabled,
                onClick = { onClick() }
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelColor,
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }

    }

}

class ButtonStyleParameterProvider : PreviewParameterProvider<ButtonStyle> {
    override val values: Sequence<ButtonStyle>
        get() = sequenceOf(*ButtonStyle.entries.toTypedArray())
}

@Preview
@Composable
fun RelayButtonWithoutIconPreview(
    @PreviewParameter(ButtonStyleParameterProvider::class) buttonStyle: ButtonStyle
) {
    WaterMyPlantsTheme {
        RelayButton(
            buttonStyle = buttonStyle,
            label = "Label"
        )
    }
}

@Preview
@Composable
fun RelayButtonWithIconPreview(
    @PreviewParameter(ButtonStyleParameterProvider::class) buttonStyle: ButtonStyle
) {
    WaterMyPlantsTheme {
        RelayButton(
            buttonStyle = buttonStyle,
            label = "Label"
        )
        RelayButton(
            buttonStyle = buttonStyle,
            label = "Label",
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = when (buttonStyle) {
                        ButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
                        ButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
                        ButtonStyle.Text -> MaterialTheme.colorScheme.primary
                        ButtonStyle.Elevated -> MaterialTheme.colorScheme.primary
                        ButtonStyle.Tonal -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    contentDescription = ""
                )
            }
        )
    }
}*/
