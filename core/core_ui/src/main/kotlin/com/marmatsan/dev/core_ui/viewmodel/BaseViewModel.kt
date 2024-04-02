package com.marmatsan.dev.core_ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.core_ui.action.Action
import com.marmatsan.dev.core_ui.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<A : Action, E : Event> : ViewModel() {

    protected val _UIEventChannel = Channel<E>()
    val UIEventFlow = _UIEventChannel.receiveAsFlow()

    protected abstract suspend fun handleAction(action: A)

    fun onAction(action: A) {
        viewModelScope.launch {
            handleAction(action)
        }
    }
}