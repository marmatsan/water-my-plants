package com.marmatsan.dev.catalog_ui.plant_screen

import com.marmatsan.dev.core_ui.event.Event
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlantScreenViewModel : BaseViewModel<PlantScreenAction, PlantScreenEvent>() {

    private val _state = MutableStateFlow(PlantScreenState())
    val state = _state.asStateFlow()

    override suspend fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddImage -> {

            }

            is PlantScreenAction.OnChangeImage -> {

            }

            is PlantScreenAction.OnRemoveImage -> {

            }

            is PlantScreenAction.OnBack -> {

            }

            is PlantScreenAction.OnCreatePlant -> {

            }

            is PlantScreenAction.OnAIButtonClick -> {

            }

            is PlantScreenAction.OnPlantSizeClick -> {
                _state.value = _state.value.copy(plantSizeDialogVisible = true)
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                _state.value = _state.value.copy(plantSizeDialogVisible = false)
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                _state.value = _state.value.copy(wateringDaysDialogVisible = false)
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                _state.value = _state.value.copy(wateringDaysDialogVisible = true)
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                _state.value = _state.value.copy(wateringHourDialogVisible = true)
            }

            is PlantScreenAction.OnPlantNameChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(name = action.plantName)
                )
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(wateringDays = action.wateringDays)
                )
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(waterAmount = action.waterAmount)
                )
            }

            is PlantScreenAction.OnSizeChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(size = action.size)
                )
            }

            is PlantScreenAction.OnDescriptionChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(description = action.description)
                )
            }

            is PlantScreenAction.OnShortDescriptionChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(shortDescription = action.shortDescription)
                )
            }
        }
    }

}

sealed interface PlantScreenEvent : Event {

}