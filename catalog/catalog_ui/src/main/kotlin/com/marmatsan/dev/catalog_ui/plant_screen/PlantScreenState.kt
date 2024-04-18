package com.marmatsan.dev.catalog_ui.plant_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class PlantScreenState(
    val plant: Plant = Plant(),
    val plantSizeDialogVisible: Boolean = false,
    val wateringDaysDialogVisible: Boolean = false,
    val wateringTimeDialogVisible: Boolean = false
)