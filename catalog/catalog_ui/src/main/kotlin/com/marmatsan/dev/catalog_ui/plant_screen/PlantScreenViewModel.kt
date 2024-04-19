package com.marmatsan.dev.catalog_ui.plant_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.core_ui.event.Event
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val plantScreenUseCases: PlantScreenUseCases,
) : BaseViewModel<PlantScreenAction, PlantScreenEvent>() {

    private val _state = MutableStateFlow(PlantScreenState())
    val state = _state.asStateFlow()

    override fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddImage -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(image = action.imageUri)
                )
            }

            is PlantScreenAction.OnRemoveImage -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(image = null)
                )
            }

            is PlantScreenAction.OnBack -> {

            }

            is PlantScreenAction.OnCreatePlant -> {
                viewModelScope.launch {
                    plantScreenUseCases.insertPlantUseCase(input = _state.value.plant)
                }
            }

            is PlantScreenAction.OnAIButtonClick -> {
                // TODO
            }

            is PlantScreenAction.OnPlantSizeClick -> {
                _state.value =
                    _state.value.copy(plantSizeDialogVisible = true)
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                _state.value =
                    _state.value.copy(plantSizeDialogVisible = false)
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                _state.value =
                    _state.value.copy(wateringDaysDialogVisible = false)
            }

            is PlantScreenAction.OnDismissWateringTimeDialog -> {
                _state.value =
                    _state.value.copy(wateringTimeDialogVisible = false)
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                _state.value =
                    _state.value.copy(wateringDaysDialogVisible = true)
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                _state.value =
                    _state.value.copy(wateringTimeDialogVisible = true)
            }

            is PlantScreenAction.OnPlantNameChange -> {
                val (isValid, plantName) = plantScreenUseCases.validatePlantNameUseCase(
                    action.plantName
                )
                if (isValid) {
                    _state.value = _state.value.copy(
                        plant = _state.value.plant.copy(name = plantName)
                    )
                }
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(wateringDays = action.wateringDays)
                )
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                val (isValid, intWaterAmount) = plantScreenUseCases.validateWaterQuantityUseCase(
                    action.waterAmount
                )
                if (isValid) {
                    _state.value = _state.value.copy(
                        plant = _state.value.plant.copy(waterAmount = intWaterAmount)
                    )
                }
            }

            is PlantScreenAction.OnWateringTimeChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(wateringTime = action.wateringTime)
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