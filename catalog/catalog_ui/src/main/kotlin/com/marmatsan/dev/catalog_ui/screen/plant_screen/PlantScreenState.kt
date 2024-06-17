package com.marmatsan.dev.catalog_ui.screen.plant_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class PlantScreenState(
    val plant: Plant = Plant(),
    val isPlantSizeDialogVisible: Boolean = false,
    val isWateringDaysDialogVisible: Boolean = false,
    val isWateringTimeDialogVisible: Boolean = false,
    val isCreatePlantButtonEnabled: Boolean = false
)