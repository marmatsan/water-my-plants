package com.marmatsan.dev.catalog_ui.screen.plant_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantDataParameters
import com.marmatsan.dev.core_ui.viewmodel.MVIViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val repository: CatalogRepository,
    private val plantScreenUseCases: PlantScreenUseCases,
    @Assisted private val savedStateHandle: SavedStateHandle
) : MVIViewModel<PlantScreenAction, PlantScreenEvent>() {

    private companion object {
        private const val PLANT_ID_KEY = "plantId"
    }

    val plantId: String? = savedStateHandle[PLANT_ID_KEY]

    private val plantScreenStateFlow = MutableStateFlow(value = PlantScreenState())

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
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = plantScreenStateFlow.value
    )

    init {
        plantId?.let {
            viewModelScope.launch {
                plantScreenStateFlow.update {
                    it.copy(plant = repository.readPlantById(plantId))
                }
            }
        }
    }

    override fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddImage -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(image = action.imageUri))
                }
            }

            is PlantScreenAction.OnRemoveImage -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(image = null))
                }
            }

            is PlantScreenAction.OnBackClick -> {
                sendEvent(event = PlantScreenEvent.BackClicked)
            }

            is PlantScreenAction.OnCreatePlant -> {
                viewModelScope.launch {
                    plantScreenUseCases.insertPlantUseCase(input = plantScreenStateFlow.value.plant)
                    sendEvent(event = PlantScreenEvent.PlantCreated)
                }
            }

            is PlantScreenAction.OnAIButtonClick -> {
                // TODO
            }

            is PlantScreenAction.OnPlantSizeClick -> {
                plantScreenStateFlow.update {
                    it.copy(isPlantSizeDialogVisible = true)
                }
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                plantScreenStateFlow.update {
                    it.copy(isPlantSizeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                plantScreenStateFlow.update {
                    it.copy(isWateringDaysDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringTimeDialog -> {
                plantScreenStateFlow.update {
                    it.copy(isWateringTimeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                plantScreenStateFlow.update {
                    it.copy(isWateringDaysDialogVisible = true)
                }
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                plantScreenStateFlow.update {
                    it.copy(isWateringTimeDialogVisible = true)
                }
            }

            // Plant properties
            is PlantScreenAction.OnPlantNameChange -> {
                val plantName = action.plantName
                if (plantScreenUseCases.validatePlantNameUseCase(plantName))
                    plantScreenStateFlow.update {
                        it.copy(plant = it.plant.copy(name = plantName.ifEmpty { null }))
                    }
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(wateringDays = action.wateringDays))
                }
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                val (isValid, waterAmount) = plantScreenUseCases.validateWaterQuantityUseCase(action.waterAmount)
                if (isValid)
                    plantScreenStateFlow.update {
                        it.copy(plant = it.plant.copy(waterAmount = waterAmount))
                    }
            }

            is PlantScreenAction.OnWateringTimeChange -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(wateringTime = action.wateringTime))
                }
            }

            is PlantScreenAction.OnSizeChange -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(size = action.size))
                }
            }

            is PlantScreenAction.OnDescriptionChange -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(description = action.description.ifEmpty { null }))
                }
            }

            is PlantScreenAction.OnShortDescriptionChange -> {
                plantScreenStateFlow.update {
                    it.copy(plant = it.plant.copy(shortDescription = action.shortDescription))
                }
            }
        }
    }

    fun setPlantId(plantId: String?) {
        savedStateHandle[PLANT_ID_KEY] = plantId
    }
}