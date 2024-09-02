package com.marmatsan.dev.core_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.core_ui.action.Action
import com.marmatsan.dev.core_ui.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<A : Action, E : Event> : ViewModel() {
    private val _uiEventChannel = Channel<E>()
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    protected abstract fun handleAction(action: A)

    fun onAction(action: A) {
        handleAction(action)
    }

    fun sendEvent(event: E) {
        viewModelScope.launch {
            _uiEventChannel.send(event)
        }
    }
}