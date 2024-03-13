package com.marmatsan.dev.core_ui.components.custom.textfield

import androidx.compose.ui.unit.Dp
import com.marmatsan.dev.core_ui.theme.ShapesDefaults

class TextFieldCornerRadii(
    textFieldStyle: TextFieldStyle
) {
    val borderRadiusTopStart: Dp = when (textFieldStyle) {
        TextFieldStyle.Filled -> ShapesDefaults.extraSmall
        TextFieldStyle.Outlined -> ShapesDefaults.extraSmall
    }
    val borderRadiusTopEnd: Dp = when (textFieldStyle) {
        TextFieldStyle.Filled -> ShapesDefaults.extraSmall
        TextFieldStyle.Outlined -> ShapesDefaults.extraSmall
    }
    val borderRadiusBottomEnd: Dp = when (textFieldStyle) {
        TextFieldStyle.Filled -> ShapesDefaults.none
        TextFieldStyle.Outlined -> ShapesDefaults.extraSmall
    }
    val borderRadiusBottomStart: Dp = when (textFieldStyle) {
        TextFieldStyle.Filled -> ShapesDefaults.none
        TextFieldStyle.Outlined -> ShapesDefaults.extraSmall
    }
}