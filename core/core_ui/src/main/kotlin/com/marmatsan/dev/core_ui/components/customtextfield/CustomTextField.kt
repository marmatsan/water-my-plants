package com.marmatsan.dev.core_ui.components.customtextfield

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(LocalTextSelectionColors.current.handleColor),
    label: (@Composable (TextStyle) -> Unit)? = null,
    supportingText: @Composable ((TextStyle) -> Unit)? = null
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding.none, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        BasicTextField(
            modifier = Modifier
                .defaultMinSize(
                    minHeight = TextFieldDefaults.MinHeight
                )
                .weight(1f),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shapes.extraSmall)
                        .background(colorScheme.secondaryContainer)
                        .padding(
                            horizontal = padding.medium,
                            vertical = padding.small
                        ),
                    contentAlignment = if (value.isNotEmpty() || isFocused)
                        Alignment.TopStart
                    else
                        Alignment.CenterStart
                ) {
                    if (value.isNotEmpty() || isFocused) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                padding.none,
                                Alignment.Top
                            ),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            label?.invoke(typography.bodySmall)
                            innerTextField()
                        }
                    } else {
                        label?.invoke(typography.bodyLarge)
                    }
                }
            }
        )
        supportingText?.let {
            Box(
                modifier = Modifier
                    .padding(
                        start = padding.medium,
                        top = padding.extraSmall,
                        end = padding.medium,
                        bottom = padding.none
                    )
                    .wrapContentHeight()
            ) {
                supportingText.invoke(typography.bodySmall)
            }
        }
    }
}

@Preview(
    widthDp = 452,
    heightDp = 96
)
@Composable
private fun CustomTextFieldPreview() {
    WaterMyPlantsTheme {
        CustomTextField(
            value = String.Empty,
            onValueChange = {},
            label = { textStyle ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Label",
                    style = textStyle
                )
            }
        )
    }
}

@Preview(
    widthDp = 452,
    heightDp = 96
)
@Composable
private fun CustomTextFieldPreviewWithValue() {
    WaterMyPlantsTheme {
        CustomTextField(
            value = "Plant description",
            onValueChange = {},
            label = { textStyle ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Label",
                    style = textStyle
                )
            },
            supportingText = { textStyle ->
                Text(
                    text = "100/5000",
                    style = textStyle
                )
            }
        )
    }
}