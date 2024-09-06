package com.marmatsan.dev.catalog_ui.screen.home_screen

import com.marmatsan.dev.core_ui.event.Event

sealed interface HomeScreenEvent : Event {
    data object NavigateToPlantScreen : HomeScreenEvent
    data class NavigateToDetailScreen(val plantId : String) : HomeScreenEvent
}