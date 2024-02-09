package com.marmatsan.dev.watermyplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashScreenViewModel : ViewModel() {

    private val _isReady = MutableStateFlow(value = false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1300L)
            _isReady.value = true
        }
    }
}