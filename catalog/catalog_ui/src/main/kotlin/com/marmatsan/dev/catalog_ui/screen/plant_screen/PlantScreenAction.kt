package com.marmatsan.dev.catalog_ui.screen.plant_screen

import android.net.Uri
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_ui.action.Action
import java.time.DayOfWeek
import java.time.LocalTime

sealed interface PlantScreenAction : Action {
    data class OnAddImage(val imageUri: Uri?) : PlantScreenAction
    data object OnRemoveImage : PlantScreenAction
    data object OnBack : PlantScreenAction
    data object OnCreatePlant : PlantScreenAction
    data object OnAIButtonClick : PlantScreenAction
    data object OnPlantSizeClick : PlantScreenAction
    data object OnDismissPlantSizeDialog : PlantScreenAction
    data object OnDismissWateringDaysDialog : PlantScreenAction
    data object OnDismissWateringTimeDialog : PlantScreenAction
    data object OnWateringDaysClick : PlantScreenAction
    data object OnWateringTimeClick : PlantScreenAction

    // Related to plant properties
    data class OnPlantNameChange(val plantName: String) : PlantScreenAction
    data class OnWateringDaysChange(val wateringDays: List<DayOfWeek>?) : PlantScreenAction
    data class OnWaterAmountChange(val waterAmount: String) : PlantScreenAction
    data class OnWateringTimeChange(val wateringTime: LocalTime?) : PlantScreenAction
    data class OnSizeChange(val size: PlantSize?) : PlantScreenAction
    data class OnDescriptionChange(val description: String) : PlantScreenAction
    data class OnShortDescriptionChange(val shortDescription: String) : PlantScreenAction
}