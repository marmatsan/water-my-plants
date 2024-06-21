package com.marmatsan.dev.catalog_ui.screen.plant_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantDataParameters
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val plantScreenUseCases: PlantScreenUseCases
) : BaseViewModel<PlantScreenAction, PlantScreenEvent>() {

    private val plantScreenStateFlow = MutableStateFlow(PlantScreenState())

    val state = plantScreenStateFlow.map { plantScreenState ->
        val isCreatePlantButtonEnabled = plantScreenUseCases.validatePlantDataUseCase(
            ValidatePlantDataParameters(
                plantName = plantScreenState.plant.name,
                wateringDaysList = plantScreenState.plant.wateringDays,
                wateringTime = plantScreenState.plant.wateringTime
            )
        )
        plantScreenState.copy(isCreatePlantButtonEnabled = isCreatePlantButtonEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = PlantScreenState()
    )

    override fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddImage -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(image = action.imageUri))
                }
            }

            is PlantScreenAction.OnRemoveImage -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(image = null))
                }
            }

            is PlantScreenAction.OnBack -> {

            }

            is PlantScreenAction.OnCreatePlant -> {
                viewModelScope.launch {
                    plantScreenUseCases.insertPlantUseCase(input = plantScreenStateFlow.value.plant)
                }
            }

            is PlantScreenAction.OnAIButtonClick -> {
                // TODO
            }

            is PlantScreenAction.OnPlantSizeClick -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isPlantSizeDialogVisible = true)
                }
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isPlantSizeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isWateringDaysDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringTimeDialog -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isWateringTimeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isWateringDaysDialogVisible = true)
                }
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(isWateringTimeDialogVisible = true)
                }
            }

            // Plant properties
            is PlantScreenAction.OnPlantNameChange -> {
                val plantName = action.plantName
                plantScreenUseCases.validatePlantNameUseCase(plantName).fold(
                    onSuccess = {
                        plantScreenStateFlow.update { plantScreenState ->
                            plantScreenState.copy(plant = plantScreenState.plant.copy(name = plantName.ifEmpty { null }))
                        }
                    },
                    onError = {

                    }
                )
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(wateringDays = action.wateringDays))
                }
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                plantScreenUseCases.validateWaterQuantityUseCase(action.waterAmount).fold(
                    onSuccess = { waterAmount ->
                        plantScreenStateFlow.update { plantScreenState ->
                            plantScreenState.copy(plant = plantScreenState.plant.copy(waterAmount = waterAmount))
                        }
                    },
                    onError = {

                    }
                )
            }

            is PlantScreenAction.OnWateringTimeChange -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(wateringTime = action.wateringTime))
                }
            }

            is PlantScreenAction.OnSizeChange -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(size = action.size))
                }
            }

            is PlantScreenAction.OnDescriptionChange -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(description = action.description.ifEmpty { null }))
                }
            }

            is PlantScreenAction.OnShortDescriptionChange -> {
                plantScreenStateFlow.update { plantScreenState ->
                    plantScreenState.copy(plant = plantScreenState.plant.copy(shortDescription = action.shortDescription))
                }
            }
        }
    }
}