package com.marmatsan.dev.catalog_ui.screen.plant_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantDataParameters
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val plantScreenUseCases: PlantScreenUseCases
) : BaseViewModel<PlantScreenAction, PlantScreenEvent>() {

    private val plantScreenStateFlow = MutableStateFlow(PlantScreenState())

    private val isCreatePlantButtonEnabledFlow = combine(
        plantScreenStateFlow
    ) {
        val plantScreenState = it.first()
        plantScreenUseCases.validatePlantDataUseCase(
            ValidatePlantDataParameters(
                plantName = plantScreenState.plant.name,
                wateringDaysList = plantScreenState.plant.wateringDays,
                wateringTime = plantScreenState.plant.wateringTime
            )
        )
    }

    val state = combine(
        plantScreenStateFlow,
        isCreatePlantButtonEnabledFlow
    ) { plantScreenState, isCreatePlantButtonEnabled ->
        plantScreenState.copy(isCreatePlantButtonEnabled = isCreatePlantButtonEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = PlantScreenState()
    )

    override fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddImage -> {
                plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                    plant = plantScreenStateFlow.value.plant.copy(image = action.imageUri)
                )
            }

            is PlantScreenAction.OnRemoveImage -> {
                plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                    plant = plantScreenStateFlow.value.plant.copy(image = null)
                )
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
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isPlantSizeDialogVisible = true)
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isPlantSizeDialogVisible = false)
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isWateringDaysDialogVisible = false)
            }

            is PlantScreenAction.OnDismissWateringTimeDialog -> {
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isWateringTimeDialogVisible = false)
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isWateringDaysDialogVisible = true)
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                plantScreenStateFlow.value =
                    plantScreenStateFlow.value.copy(isWateringTimeDialogVisible = true)
            }

            is PlantScreenAction.OnPlantNameChange -> {
                val plantName = action.plantName
                plantScreenUseCases.validatePlantNameUseCase(plantName).fold(
                    onSuccess = {
                        plantScreenStateFlow.update { plantScreenState ->
                            plantScreenState.copy(plant = plantScreenState.plant.copy(name = plantName))
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
                        plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                            plant = plantScreenStateFlow.value.plant.copy(waterAmount = waterAmount)
                        )
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
                plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                    plant = plantScreenStateFlow.value.plant.copy(size = action.size)
                )
            }

            is PlantScreenAction.OnDescriptionChange -> {
                plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                    plant = plantScreenStateFlow.value.plant.copy(description = action.description)
                )
            }

            is PlantScreenAction.OnShortDescriptionChange -> {
                plantScreenStateFlow.value = plantScreenStateFlow.value.copy(
                    plant = plantScreenStateFlow.value.plant.copy(shortDescription = action.shortDescription)
                )
            }
        }
    }
}