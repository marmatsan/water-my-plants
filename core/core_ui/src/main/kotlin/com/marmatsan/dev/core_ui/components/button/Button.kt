package com.marmatsan.dev.core_ui.components.button

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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

@Composable
fun Button(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.Filled,
    colors: ButtonColors = when (buttonStyle) {
        ButtonStyle.Filled -> ButtonDefaults.buttonColors()
        ButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors()
        ButtonStyle.Text -> ButtonDefaults.textButtonColors()
        ButtonStyle.Elevated -> ButtonDefaults.elevatedButtonColors()
        ButtonStyle.Tonal -> ButtonDefaults.filledTonalButtonColors()
    },
    labelText: String = String.Empty,
    labelTextColor: Color = when (buttonStyle) {
        ButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
        ButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
        ButtonStyle.Text -> MaterialTheme.colorScheme.primary
        ButtonStyle.Elevated -> MaterialTheme.colorScheme.primary
        ButtonStyle.Tonal -> MaterialTheme.colorScheme.onSecondaryContainer
    },
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    val buttonModifier = modifier.height(ButtonDefaults.MinHeight)

    when (buttonStyle) {
        ButtonStyle.Filled ->
            Button(
                modifier = buttonModifier,
                enabled = enabled,
                colors = colors,
                onClick = onClick
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelTextColor,
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Outlined ->
            OutlinedButton(
                modifier = buttonModifier,
                enabled = enabled,
                colors = colors,
                onClick = onClick
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelTextColor,
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Text ->
            TextButton(
                modifier = buttonModifier,
                enabled = enabled,
                colors = colors,
                onClick = onClick
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelTextColor,
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge
                )
            }

        ButtonStyle.Elevated ->
            ElevatedButton(
                modifier = buttonModifier,
                enabled = enabled,
                colors = colors,
                onClick = onClick
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelTextColor,
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge
                )
            }


        ButtonStyle.Tonal ->
            FilledTonalButton(
                modifier = buttonModifier,
                enabled = enabled,
                colors = colors,
                onClick = onClick
            ) {
                icon?.invoke()
                Text(
                    modifier = Modifier.padding(
                        start = if (icon != null) spacing.small else spacing.default
                    ),
                    color = labelTextColor,
                    text = labelText,
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
        Button(
            buttonStyle = buttonStyle,
            labelText = "Label"
        )
    }
}

@Preview
@Composable
fun RelayButtonWithIconPreview(
    @PreviewParameter(ButtonStyleParameterProvider::class) buttonStyle: ButtonStyle
) {
    WaterMyPlantsTheme {
        Button(
            buttonStyle = buttonStyle,
            labelText = "Label"
        )
        Button(
            buttonStyle = buttonStyle,
            labelText = "Label",
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
}
