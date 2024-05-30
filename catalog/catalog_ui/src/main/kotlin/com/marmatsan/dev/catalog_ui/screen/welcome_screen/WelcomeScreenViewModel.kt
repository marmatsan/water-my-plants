package com.marmatsan.dev.catalog_ui.screen.welcome_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class WelcomeScreenViewModel(
    private val repository: CatalogRepository
) : BaseViewModel<WelcomeScreenAction, WelcomeScreenEvent>() {

    private val _state = MutableStateFlow(WelcomeScreenState())
    val state = _state.asStateFlow()

    override fun handleAction(action: WelcomeScreenAction) {
        when (action) {
            WelcomeScreenAction.OnAddFirstPlantClick -> {
                viewModelScope.launch {
                    repository.saveIsPlantNameValid(false)
                    repository.saveIsWateringDaysValid(false)
                    repository.saveIsWateringTimeValid(false)
                    _state.value = state.value.copy(finishedResettingPreferences = true)
                }
            }
        }
    }

}