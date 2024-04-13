package com.marmatsan.dev.core_ui.components.customtextfield

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomTextField(
    modifier: Modifier
) {
    val state = rememberTextFieldState()

    BasicTextField2(
        modifier = modifier,
        state = state
    )
}