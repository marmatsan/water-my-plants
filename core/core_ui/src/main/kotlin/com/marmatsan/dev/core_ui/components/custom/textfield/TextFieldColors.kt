package com.marmatsan.dev.core_ui.components.custom.textfield

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class TextFieldColors(
    private val textFieldStyle: TextFieldStyle
) {
    val focusedTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val unfocusedTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val disabledTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val focusedContainerColor: Color
        @Composable get() = when (textFieldStyle) {
            TextFieldStyle.Filled -> MaterialTheme.colorScheme.surfaceContainerHighest
            TextFieldStyle.Outlined -> Color.Transparent
        }
    val unfocusedContainerColor: Color
        @Composable get() = when (textFieldStyle) {
            TextFieldStyle.Filled -> MaterialTheme.colorScheme.surfaceContainerHighest
            TextFieldStyle.Outlined -> Color.Transparent
        }
    val disabledContainerColor: Color
        @Composable get() = when (textFieldStyle) {
            TextFieldStyle.Filled -> MaterialTheme.colorScheme.onSurface
            TextFieldStyle.Outlined -> Color.Transparent
        }
    val errorContainerColor: Color
        @Composable get() = when (textFieldStyle) {
            TextFieldStyle.Filled -> MaterialTheme.colorScheme.surfaceContainerHighest
            TextFieldStyle.Outlined -> Color.Transparent
        }
    val cursorColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val errorCursorColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedIndicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val unfocusedIndicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledIndicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorIndicatorColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedLeadingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedLeadingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledLeadingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorLeadingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val focusedTrailingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedTrailingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledTrailingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorTrailingIconColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedLabelColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val unfocusedLabelColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledLabelColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorLabelColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedPlaceholderColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedPlaceholderColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledPlaceholderColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorPlaceholderColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val focusedSupportingTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedSupportingTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledSupportingTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val errorSupportingTextColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedPrefixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedPrefixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledPrefixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorPrefixColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val focusedSuffixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val unfocusedSuffixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledSuffixColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val errorSuffixColor: Color
        @Composable get() = MaterialTheme.colorScheme.error
}