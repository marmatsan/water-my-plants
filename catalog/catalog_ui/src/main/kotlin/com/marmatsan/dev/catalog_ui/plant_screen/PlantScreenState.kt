package com.marmatsan.dev.catalog_ui.plant_screen

import com.marmatsan.dev.catalog_domain.model.Plant

data class PlantScreenState(
    val plant: Plant = Plant(),
    @Suppress("PropertyName")
    val AIButtonAvailable: Boolean = false,
    @Suppress("PropertyName")
    val AIActionsAvailable: Boolean = false,
    val scrollIndicatorVisible: Boolean = false,
    val removePhotoAvailable: Boolean = false,
    val plantSizeDialogVisible: Boolean = false,
    val wateringDaysDialogVisible: Boolean = false,
    val wateringTimeDialogVisible: Boolean = false
)