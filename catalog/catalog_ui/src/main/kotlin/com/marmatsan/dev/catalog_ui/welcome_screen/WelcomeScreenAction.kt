package com.marmatsan.dev.catalog_ui.welcome_screen

import com.marmatsan.dev.core_ui.action.Action

sealed interface WelcomeScreenAction : Action {
    data object OnAddFirstPlantClick : WelcomeScreenAction
}