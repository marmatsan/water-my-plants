package com.marmatsan.dev.core_ui.components.custom.textfield

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String = String.Empty,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textFieldStyle: TextFieldStyle = TextFieldStyle.Filled,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = 1,
    minLines: Int = 1,
    textFieldColors: TextFieldColors = when (textFieldStyle) {
        TextFieldStyle.Filled -> TextFieldDefaults.colors()
        TextFieldStyle.Outlined -> OutlinedTextFieldDefaults.colors()
    },
    textFieldCornerRadii: TextFieldCornerRadii = TextFieldCornerRadii(
        textFieldStyle
    )
) {
    when (textFieldStyle) {
        TextFieldStyle.Filled -> TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            shape = RoundedCornerShape(
                topStart = textFieldCornerRadii.borderRadiusTopStart,
                topEnd = textFieldCornerRadii.borderRadiusTopEnd,
                bottomEnd = textFieldCornerRadii.borderRadiusBottomEnd,
                bottomStart = textFieldCornerRadii.borderRadiusBottomStart
            ),
            colors = textFieldColors
        )

        TextFieldStyle.Outlined -> OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            shape = RoundedCornerShape(
                topStart = textFieldCornerRadii.borderRadiusTopStart,
                topEnd = textFieldCornerRadii.borderRadiusTopEnd,
                bottomEnd = textFieldCornerRadii.borderRadiusBottomEnd,
                bottomStart = textFieldCornerRadii.borderRadiusBottomStart
            ),
            colors = textFieldColors
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
private fun TextFieldDisabledTextPreview(
    @PreviewParameter(TextFieldStyleParameterProvider::class) textFieldStyle: TextFieldStyle
) {
    WaterMyPlantsTheme {
        TextField(
            textFieldStyle = textFieldStyle,
            label = { Text("Label") },
            readOnly = true,
            enabled = false,
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

@Preview
@Composable
private fun TextFieldOverridenDisabledTextPreview(
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

