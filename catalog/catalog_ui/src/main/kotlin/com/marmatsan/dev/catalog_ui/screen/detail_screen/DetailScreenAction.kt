package com.marmatsan.dev.catalog_ui.screen.detail_screen

import com.marmatsan.dev.core_ui.action.Action

sealed interface DetailScreenAction : Action {
    data object OnBackClick : DetailScreenAction
    data object OnDropdownMenuClick : DetailScreenAction
    data object OnEditPlantClick : DetailScreenAction
    data object OnDeletePlantClick : DetailScreenAction
}