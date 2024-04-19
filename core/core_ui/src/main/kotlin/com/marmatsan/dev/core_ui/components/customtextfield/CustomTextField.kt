package com.marmatsan.dev.core_ui.components.customtextfield

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null, // TODO
    bodyTextStyle: TextStyle = TextStyle.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    // TODO: Change with BasicTextField2 when stable
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        modifier = modifier.defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight
        ),
        value = value,
        onValueChange = onValueChange,
        textStyle = bodyTextStyle,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Box(
                    modifier = modifier
                        .clip(shapes.extraSmall)
                        .background(colorScheme.secondaryContainer)
                        .padding(
                            horizontal = spacing.medium,
                            vertical = spacing.extraSmall
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    label?.invoke()
                }
            } else {
                Box(
                    modifier = modifier
                        .clip(shapes.extraSmall)
                        .background(colorScheme.secondaryContainer)
                        .padding(
                            horizontal = spacing.medium,
                            vertical = spacing.extraSmall
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = spacing.extraSmall)
                    ) {
                        label?.invoke()
                        innerTextField()
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun CustomTextFieldPreview() {
    WaterMyPlantsTheme {
        CustomTextField(
            modifier = Modifier
                .width(TextFieldDefaults.MinWidth)
                .height(TextFieldDefaults.MinHeight),
            value = "Body",
            onValueChange = {},
            bodyTextStyle = MaterialTheme.typography.bodyLarge,
            label = {
                Text(
                    text = "Label",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}