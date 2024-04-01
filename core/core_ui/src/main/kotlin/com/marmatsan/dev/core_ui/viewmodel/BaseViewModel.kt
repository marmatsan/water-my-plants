package com.marmatsan.dev.core_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.core_ui.event.Action
import com.marmatsan.dev.core_ui.event.UiEvent
import com.marmatsan.dev.core_ui.text.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<A : Action> : ViewModel() {

    protected val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    protected abstract suspend fun handleAction(action: A)

    fun onAction(action: A) {
        viewModelScope.launch {
            handleAction(action)
        }
    }

    protected suspend fun sendSuccessEvent() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.Success)
        }
    }

    protected suspend fun sendErrorEvent(message: UiText) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackBar(message))
        }
    }
}