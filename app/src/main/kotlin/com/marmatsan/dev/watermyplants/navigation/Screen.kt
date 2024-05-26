package com.marmatsan.dev.watermyplants.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object WelcomeScreen : Screen
    @Serializable
    data object PlantScreen : Screen
    @Serializable
    data object HomeScreen : Screen
    @Serializable
    data object DetailScreen : Screen
}