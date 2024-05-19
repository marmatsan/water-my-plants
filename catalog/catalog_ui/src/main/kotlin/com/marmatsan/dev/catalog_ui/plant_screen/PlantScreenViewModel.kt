package com.marmatsan.dev.catalog_ui.plant_screen

import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantDataParameters
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val plantScreenUseCases: PlantScreenUseCases,
    private val repository: CatalogRepository
) : BaseViewModel<PlantScreenAction, PlantScreenEvent>() {

    init {
        viewModelScope.launch {
            repository.saveIsPlantNameValid(false)
            repository.saveIsWateringDaysValid(false)
            repository.saveIsWateringTimeValid(false)
        }
    }

    private val _state = MutableStateFlow(PlantScreenState())

    private val plantScreenStateFlow = _state.asStateFlow()
    private val preferencesFlow = repository.getPreferencesFlow()

    val state =
        combine(plantScreenStateFlow, preferencesFlow) { plantScreenState, preferencesData ->
            val createPlantButtonIsEnabledResult = plantScreenUseCases.validatePlantDataUseCase(
                input = ValidatePlantDataParameters(
                    isPlantNameValid = preferencesData.isPlantNameValid,
                    isWateringDaysValid = preferencesData.isWateringDaysValid,
                    isWateringTimeValid = preferencesData.isWateringTimeValid
                )
            )
            PlantScreenState(
                plant = plantScreenState.plant,
                plantSizeDialogVisible = plantScreenState.plantSizeDialogVisible,
                wateringDaysDialogVisible = plantScreenState.wateringDaysDialogVisible,
                wateringTimeDialogVisible = plantScreenState.wateringTimeDialogVisible,
                createPlantButtonIsEnabled = when (createPlantButtonIsEnabledResult) {
                    is Result.Success -> true
                    is Result.Error -> false
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlantScreenState()
        )

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
                val plantName = action.plantName
                plantScreenUseCases.validatePlantNameUseCase(plantName).fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            plant = _state.value.plant.copy(name = plantName)
                        )
                        viewModelScope.launch {
                            repository.saveIsPlantNameValid(plantName.isNotEmpty())
                        }
                    },
                    onError = {
                        viewModelScope.launch {
                            repository.saveIsPlantNameValid(false)
                        }
                    }
                )
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(wateringDays = action.wateringDays)
                )
                viewModelScope.launch {
                    repository.saveIsWateringDaysValid(true)
                }
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                plantScreenUseCases.validateWaterQuantityUseCase(action.waterAmount).fold(
                    onSuccess = { waterAmount ->
                        _state.value = _state.value.copy(
                            plant = _state.value.plant.copy(waterAmount = waterAmount)
                        )
                    },
                    onError = {}
                )
            }

            is PlantScreenAction.OnWateringTimeChange -> {
                _state.value = _state.value.copy(
                    plant = _state.value.plant.copy(wateringTime = action.wateringTime)
                )
                viewModelScope.launch {
                    repository.saveIsWateringTimeValid(true)
                }
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