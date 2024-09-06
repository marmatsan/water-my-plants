package com.marmatsan.dev.catalog_ui.screen.plant_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantDataParameters
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_domain.preferences.Preferences
import com.marmatsan.dev.core_domain.updateState
import com.marmatsan.dev.core_ui.viewmodel.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class PlantScreenViewModel(
    private val repository: CatalogRepository,
    private val plantScreenUseCases: PlantScreenUseCases,
    private val preferences: Preferences,
    @Assisted private val savedStateHandle: SavedStateHandle
) : MviViewModel<PlantScreenAction, PlantScreenEvent>() {

    private companion object {
        private const val PLANT_ID_KEY = "plantId"
        private const val MEDIA_PERMISSION_DECLINED_COUNT_KEY = "mediaPermissionDeclinedCount"
    }

    val plantId: String? = savedStateHandle[PLANT_ID_KEY]

    // State
    private val plantScreenStateFlow = MutableStateFlow(value = PlantScreenState())

    val state = plantScreenStateFlow.map { plantScreenState ->
        val isCreatePlantButtonEnabled = plantScreenUseCases.validatePlantDataUseCase(
            ValidatePlantDataParameters(
                plantName = plantScreenState.plant.name,
                wateringDaysList = plantScreenState.plant.wateringDays,
                wateringTime = plantScreenState.plant.wateringTime
            )
        )
        plantScreenState.copy(
            isCreatePlantButtonEnabled = isCreatePlantButtonEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 5_000
        ),
        initialValue = plantScreenStateFlow.value
    )

    init {
        plantId?.let {
            viewModelScope.launch {
                plantScreenStateFlow.updateState {
                    copy(plant = repository.readPlantById(plantId))
                }
            }
        }
        savedStateHandle[MEDIA_PERMISSION_DECLINED_COUNT_KEY] = 0
    }

    override fun handleAction(action: PlantScreenAction) {
        when (action) {
            is PlantScreenAction.OnAddOrChangeImage -> {
                viewModelScope.launch {
                    val isMediaAccessPermanentlyDeclined =
                        preferences.readIsMediaAccessPermanentlyDeclined()
                    if (isMediaAccessPermanentlyDeclined)
                        sendEvent(PlantScreenEvent.ShowExplanatorySnackbar)
                    else
                        sendEvent(PlantScreenEvent.RequestPermission(Permission.READ_MEDIA_IMAGES))
                }
            }

            is PlantScreenAction.OnImageChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(image = action.imageUri))
                }
            }

            is PlantScreenAction.OnRemoveImage -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(image = null))
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
                plantScreenStateFlow.updateState {
                    copy(isPlantSizeDialogVisible = true)
                }
            }

            is PlantScreenAction.OnDismissPlantSizeDialog -> {
                plantScreenStateFlow.updateState {
                    copy(isPlantSizeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringDaysDialog -> {
                plantScreenStateFlow.updateState {
                    copy(isWateringDaysDialogVisible = false)
                }
            }

            is PlantScreenAction.OnDismissWateringTimeDialog -> {
                plantScreenStateFlow.updateState {
                    copy(isWateringTimeDialogVisible = false)
                }
            }

            is PlantScreenAction.OnWateringDaysClick -> {
                plantScreenStateFlow.updateState {
                    copy(isWateringDaysDialogVisible = true)
                }
            }

            is PlantScreenAction.OnWateringTimeClick -> {
                plantScreenStateFlow.updateState {
                    copy(isWateringTimeDialogVisible = true)
                }
            }

            // Plant properties
            is PlantScreenAction.OnPlantNameChange -> {
                val plantName = action.plantName
                if (plantScreenUseCases.validatePlantNameUseCase(plantName))
                    plantScreenStateFlow.updateState {
                        copy(plant = plant.copy(name = plantName.ifEmpty { null }))
                    }
            }

            is PlantScreenAction.OnWateringDaysChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(wateringDays = action.wateringDays))
                }
            }

            is PlantScreenAction.OnWaterAmountChange -> {
                val (isValid, waterAmount) = plantScreenUseCases.validateWaterQuantityUseCase(action.waterAmount)
                if (isValid)
                    plantScreenStateFlow.updateState {
                        copy(plant = plant.copy(waterAmount = waterAmount))
                    }
            }

            is PlantScreenAction.OnWateringTimeChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(wateringTime = action.wateringTime))
                }
            }

            is PlantScreenAction.OnSizeChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(size = action.size))
                }
            }

            is PlantScreenAction.OnDescriptionChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(description = action.description.ifEmpty { null }))
                }
            }

            is PlantScreenAction.OnShortDescriptionChange -> {
                plantScreenStateFlow.updateState {
                    copy(plant = plant.copy(shortDescription = action.shortDescription))
                }
            }

            is PlantScreenAction.OnRetryRequestPermission -> {
                plantScreenStateFlow.updateState {
                    copy(isMediaPermissionRationaleVisible = false)
                }
                sendEvent(PlantScreenEvent.RequestPermission(action.permission))
            }
        }
    }

    fun setPlantId(plantId: String?) {
        savedStateHandle[PLANT_ID_KEY] = plantId
    }

    fun onPermissionResultChanged(
        permission: Permission,
        isPermissionGranted: Boolean
    ) {
        if (isPermissionGranted) {
            when (permission) {
                Permission.READ_MEDIA_IMAGES -> sendEvent(PlantScreenEvent.LaunchImagePicker)
            }
        } else {
            val mediaPermissionDeclinedCount =
                savedStateHandle.get<Int>(MEDIA_PERMISSION_DECLINED_COUNT_KEY)

            if (mediaPermissionDeclinedCount.isNotNull()) {
                val currentDeclinedCount = mediaPermissionDeclinedCount + 1
                if (currentDeclinedCount == 1) {
                    plantScreenStateFlow.updateState {
                        copy(isMediaPermissionRationaleVisible = true)
                    }
                } else {
                    viewModelScope.launch {
                        preferences.saveIsMediaAccessPermanentlyDeclined(permanentlyDeclined = true)
                        sendEvent(PlantScreenEvent.ShowExplanatorySnackbar)
                    }
                }
                savedStateHandle[MEDIA_PERMISSION_DECLINED_COUNT_KEY] = currentDeclinedCount
            }
        }
    }
}

enum class Permission {
    READ_MEDIA_IMAGES
}