package com.marmatsan.dev.core_ui.components.textfield

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.extension.bringIntoViewRequester
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

enum class TextFieldStyle {
    Filled,
    Outlined
}

@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String = String.Empty,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    textFieldStyle: TextFieldStyle = TextFieldStyle.Filled,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    textFieldColors: TextFieldColors = when (textFieldStyle) {
        TextFieldStyle.Filled -> TextFieldDefaults.colors()
        TextFieldStyle.Outlined -> OutlinedTextFieldDefaults.colors()
    },
    textFieldShape: Shape = when (textFieldStyle) {
        TextFieldStyle.Filled -> TextFieldDefaults.shape
        TextFieldStyle.Outlined -> OutlinedTextFieldDefaults.shape
    },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {

    val textFieldModifier = modifier.bringIntoViewRequester()

    when (textFieldStyle) {
        TextFieldStyle.Filled -> TextField(
            modifier = textFieldModifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            shape = textFieldShape,
            colors = textFieldColors,
            interactionSource = interactionSource
        )

        TextFieldStyle.Outlined -> OutlinedTextField(
            modifier = textFieldModifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            shape = textFieldShape,
            colors = textFieldColors,
            interactionSource = interactionSource
        )
    }
}

class TextFieldStyleParameterProvider : PreviewParameterProvider<TextFieldStyle> {
    override val values: Sequence<TextFieldStyle>
        get() = sequenceOf(*TextFieldStyle.entries.toTypedArray())
}

@Preview
@Composable
private fun TextFieldLabelTextPreview(
    @PreviewParameter(TextFieldStyleParameterProvider::class) textFieldStyle: TextFieldStyle
) {
    WaterMyPlantsTheme {
        TextField(
            textFieldStyle = textFieldStyle,
            label = { Text("Label") },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Search,
                    contentDescription = ""
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = ""
                )
            }
        )
    }
}

@Preview
@Composable
private fun TextFieldInputTextPreview(
    @PreviewParameter(TextFieldStyleParameterProvider::class) textFieldStyle: TextFieldStyle
) {
    WaterMyPlantsTheme {
        TextField(
            textFieldStyle = textFieldStyle,
            label = { Text("Label") },
            value = "Input",
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Search,
                    contentDescription = ""
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = ""
                )
            }
        )
    }
}

