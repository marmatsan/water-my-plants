package com.marmatsan.dev.watermyplants.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marmatsan.dev.catalog_ui.home_screen.HomeScreen
import com.marmatsan.dev.catalog_ui.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.welcome_screen.WelcomeScreen
import com.marmatsan.dev.watermyplants.MainActivityComponent
import com.marmatsan.dev.watermyplants.create
import com.marmatsan.dev.watermyplants.di.applicationComponent

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.WelcomeScreen
) {
    val mainActivityComponent =
        MainActivityComponent::class.create(LocalContext.current.applicationComponent)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.WelcomeScreen> {
            WelcomeScreen(
                onCreatePlantClick = { navController.navigate(Screen.PlantScreen) }
            )
        }
        composable<Screen.PlantScreen> {
            PlantScreenRoot(
                viewModel = viewModel { mainActivityComponent.plantScreenViewModel },
                onCreatePlantClick = { navController.navigate(Screen.HomeScreen) }
            )
        }
        composable<Screen.HomeScreen> {
            HomeScreen()
        }
    }
}