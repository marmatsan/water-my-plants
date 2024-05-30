package com.marmatsan.dev.catalog_ui.screen.detail_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class DetailScreenViewModel(
    private val repository: CatalogRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<DetailScreenAction, DetailScreenEvent>() {

    private val plantId: String = checkNotNull(savedStateHandle["plantId"])

    private val _state = MutableStateFlow(DetailScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.readPlantById(plantId).collectLatest { plant ->
                _state.value = _state.value.copy(plant = plant)
                _state.value = _state.value.copy(isLoadingPlant = false)
            }
        }
    }

    override fun handleAction(action: DetailScreenAction) {
        when (action) {
            DetailScreenAction.OnBackClick -> {

            }

            DetailScreenAction.OnDropdownMenuClick -> {
                _state.value =
                    _state.value.copy(isDropDownMenuVisible = !_state.value.isDropDownMenuVisible)
            }

            DetailScreenAction.OnDeletePlantClick -> {
                viewModelScope.launch {
                    repository.deletePlant(_state.value.plant)
                }
            }

            DetailScreenAction.OnEditPlantClick -> {

            }
        }
    }

    fun setPlantId(plantId: String) {
        savedStateHandle["plantId"] = plantId
    }

}