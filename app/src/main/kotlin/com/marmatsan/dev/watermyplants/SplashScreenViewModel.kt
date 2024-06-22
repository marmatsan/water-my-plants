package com.marmatsan.dev.watermyplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_ui.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

data class SplashScreenState(
    val isReady: Boolean = false,
    val startDestination: Screen = Screen.WelcomeScreen
)

@Inject
class SplashScreenViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.readAllPlantsFlow().collectLatest { plants ->
                var initialScreen: Screen = Screen.WelcomeScreen
                if (plants.isNotEmpty()) {
                    initialScreen = Screen.HomeScreen
                }
                _state.update {
                    it.copy(isReady = true, startDestination = initialScreen)
                }
            }
        }
    }
}

class SplashScreenViewModelFactory(
    private val splashScreenViewModel: SplashScreenViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return splashScreenViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}