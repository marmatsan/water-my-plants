package com.marmatsan.dev.catalog_ui.screen.home_screen

import com.marmatsan.dev.core_ui.action.Action

sealed interface HomeScreenAction : Action{
    data object OnCreatePlant : HomeScreenAction
    data class OnNavigateToDetailScreen(val plantId : String) : HomeScreenAction
}