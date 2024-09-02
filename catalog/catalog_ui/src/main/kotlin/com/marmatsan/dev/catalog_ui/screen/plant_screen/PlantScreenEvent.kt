package com.marmatsan.dev.catalog_ui.screen.plant_screen

import com.marmatsan.dev.core_ui.event.Event

sealed interface PlantScreenEvent : Event {
    data object PlantCreated : PlantScreenEvent
    data object BackClicked : PlantScreenEvent
    data class RequestPermission(val permission : Permission) : PlantScreenEvent
    data object ShowExplanatorySnackbar : PlantScreenEvent
    data object LaunchImagePicker : PlantScreenEvent
    data class PermissionResultChanged(
        val permission: Permission,
        val isPermissionGranted: Boolean
    ) : PlantScreenEvent
}