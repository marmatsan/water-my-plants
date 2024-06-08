package com.marmatsan.dev.core_ui.components.customtextfield

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.extension.bringIntoViewRequester
import com.marmatsan.dev.core_ui.extension.clearFocusOnKeyboardDismiss
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: (@Composable (TextStyle) -> Unit)? = null,
    supportingText: @Composable ((TextStyle) -> Unit)? = null,
    singleLine: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = modifier
            .clearFocusOnKeyboardDismiss()
            .bringIntoViewRequester(),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        BasicTextField(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = TextFieldDefaults.MinHeight
                )
                .height(IntrinsicSize.Max)
                .weight(1f),
            value = value,
            onValueChange = onValueChange,
            textStyle = typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .clip(shapes.extraSmall)
                        .background(colorScheme.secondaryContainer)
                        .padding(
                            horizontal = spacing.medium,
                            vertical = spacing.extraSmall
                        ),
                    contentAlignment = if (value.isNotEmpty() || isFocused) Alignment.TopStart else Alignment.Center
                ) {
                    if (isFocused || value.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                spacing.default,
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
            },
            interactionSource = interactionSource,
        )
        supportingText?.let {
            Box(
                modifier = Modifier
                    .padding(
                        start = spacing.medium,
                        top = spacing.extraSmall,
                        end = spacing.medium,
                        bottom = spacing.default
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
            modifier = Modifier.fillMaxSize(),
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