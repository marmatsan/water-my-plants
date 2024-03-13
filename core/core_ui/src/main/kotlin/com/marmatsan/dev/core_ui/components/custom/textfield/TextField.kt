package com.marmatsan.dev.core_ui.components.custom.textfield

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marmatsan.dev.core_domain.Empty

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
    textFieldColors: TextFieldColors = TextFieldColors(
        textFieldStyle
    ),
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
            colors = TextFieldDefaults.colors(
                // TODO: Only non-overriden relayTextFieldColors: selectionColors
                focusedTextColor = textFieldColors.focusedTextColor,
                unfocusedTextColor = textFieldColors.unfocusedTextColor,
                disabledTextColor = textFieldColors.disabledTextColor,
                errorTextColor = textFieldColors.errorTextColor,
                focusedContainerColor = textFieldColors.focusedContainerColor,
                unfocusedContainerColor = textFieldColors.unfocusedContainerColor,
                disabledContainerColor = textFieldColors.disabledContainerColor,
                errorContainerColor = textFieldColors.errorContainerColor,
                cursorColor = textFieldColors.cursorColor,
                errorCursorColor = textFieldColors.errorCursorColor,
                focusedIndicatorColor = textFieldColors.focusedIndicatorColor,
                unfocusedIndicatorColor = textFieldColors.unfocusedIndicatorColor,
                disabledIndicatorColor = textFieldColors.disabledIndicatorColor,
                errorIndicatorColor = textFieldColors.errorIndicatorColor,
                focusedLeadingIconColor = textFieldColors.focusedLeadingIconColor,
                unfocusedLeadingIconColor = textFieldColors.unfocusedLeadingIconColor,
                disabledLeadingIconColor = textFieldColors.disabledLeadingIconColor,
                errorLeadingIconColor = textFieldColors.errorLeadingIconColor,
                focusedTrailingIconColor = textFieldColors.focusedTrailingIconColor,
                unfocusedTrailingIconColor = textFieldColors.unfocusedTrailingIconColor,
                disabledTrailingIconColor = textFieldColors.disabledTrailingIconColor,
                errorTrailingIconColor = textFieldColors.errorTrailingIconColor,
                focusedLabelColor = textFieldColors.focusedLabelColor,
                unfocusedLabelColor = textFieldColors.unfocusedLabelColor,
                disabledLabelColor = textFieldColors.disabledLabelColor,
                errorLabelColor = textFieldColors.errorLabelColor,
                focusedPlaceholderColor = textFieldColors.focusedPlaceholderColor,
                unfocusedPlaceholderColor = textFieldColors.unfocusedPlaceholderColor,
                disabledPlaceholderColor = textFieldColors.disabledPlaceholderColor,
                errorPlaceholderColor = textFieldColors.errorPlaceholderColor,
                focusedSupportingTextColor = textFieldColors.focusedSupportingTextColor,
                unfocusedSupportingTextColor = textFieldColors.unfocusedSupportingTextColor,
                disabledSupportingTextColor = textFieldColors.disabledSupportingTextColor,
                errorSupportingTextColor = textFieldColors.errorSupportingTextColor,
                focusedPrefixColor = textFieldColors.focusedPrefixColor,
                unfocusedPrefixColor = textFieldColors.unfocusedPrefixColor,
                disabledPrefixColor = textFieldColors.disabledPrefixColor,
                errorPrefixColor = textFieldColors.errorPrefixColor,
                focusedSuffixColor = textFieldColors.focusedSuffixColor,
                unfocusedSuffixColor = textFieldColors.unfocusedSuffixColor,
                disabledSuffixColor = textFieldColors.disabledSuffixColor,
                errorSuffixColor = textFieldColors.errorSuffixColor
            )
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
            colors = OutlinedTextFieldDefaults.colors(
                // TODO: Only non-overriden relayTextFieldColors: selectionColors
                focusedTextColor = textFieldColors.focusedTextColor,
                unfocusedTextColor = textFieldColors.unfocusedTextColor,
                disabledTextColor = textFieldColors.disabledTextColor,
                errorTextColor = textFieldColors.errorTextColor,
                focusedContainerColor = textFieldColors.focusedContainerColor,
                unfocusedContainerColor = textFieldColors.unfocusedContainerColor,
                disabledContainerColor = textFieldColors.disabledContainerColor,
                errorContainerColor = textFieldColors.errorContainerColor,
                cursorColor = textFieldColors.cursorColor,
                errorCursorColor = textFieldColors.errorCursorColor,
                focusedLeadingIconColor = textFieldColors.focusedLeadingIconColor,
                unfocusedLeadingIconColor = textFieldColors.unfocusedLeadingIconColor,
                disabledLeadingIconColor = textFieldColors.disabledLeadingIconColor,
                errorLeadingIconColor = textFieldColors.errorLeadingIconColor,
                focusedTrailingIconColor = textFieldColors.focusedTrailingIconColor,
                unfocusedTrailingIconColor = textFieldColors.unfocusedTrailingIconColor,
                disabledTrailingIconColor = textFieldColors.disabledTrailingIconColor,
                errorTrailingIconColor = textFieldColors.errorTrailingIconColor,
                focusedLabelColor = textFieldColors.focusedLabelColor,
                unfocusedLabelColor = textFieldColors.unfocusedLabelColor,
                disabledLabelColor = textFieldColors.disabledLabelColor,
                errorLabelColor = textFieldColors.errorLabelColor,
                focusedPlaceholderColor = textFieldColors.focusedPlaceholderColor,
                unfocusedPlaceholderColor = textFieldColors.unfocusedPlaceholderColor,
                disabledPlaceholderColor = textFieldColors.disabledPlaceholderColor,
                errorPlaceholderColor = textFieldColors.errorPlaceholderColor,
                focusedSupportingTextColor = textFieldColors.focusedSupportingTextColor,
                unfocusedSupportingTextColor = textFieldColors.unfocusedSupportingTextColor,
                disabledSupportingTextColor = textFieldColors.disabledSupportingTextColor,
                errorSupportingTextColor = textFieldColors.errorSupportingTextColor
            )
        )
    }
}

