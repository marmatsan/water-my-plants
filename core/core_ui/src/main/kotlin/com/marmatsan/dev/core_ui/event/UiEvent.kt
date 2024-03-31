package com.marmatsan.dev.core_ui.event

import com.marmatsan.dev.core_ui.text.UiText

sealed interface UiEvent {
    data object Success : UiEvent
    data object Failure : UiEvent
    data class ShowSnackBar(val message: UiText) : UiEvent
}