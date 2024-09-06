package com.marmatsan.dev.core_ui.screen

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object WelcomeScreen : Screen

    @Serializable
    data class PlantScreen(val plantId: String? = null) : Screen

    @Serializable
    data object HomeScreen : Screen

    @Serializable
    data class DetailScreen(val plantId: String) : Screen
}