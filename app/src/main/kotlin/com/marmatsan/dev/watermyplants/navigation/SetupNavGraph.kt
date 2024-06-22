package com.marmatsan.dev.watermyplants.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenRoot
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenRoot
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.screen.welcome_screen.WelcomeScreen
import com.marmatsan.dev.watermyplants.MainActivityComponent

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.PlantScreen,
    mainActivityComponent: MainActivityComponent
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.WelcomeScreen> {
            WelcomeScreen(
                navigate = { navController.navigate(Screen.PlantScreen) }
            )
        }
        composable<Screen.PlantScreen> {
            PlantScreenRoot(
                viewModel = viewModel { mainActivityComponent.plantScreenViewModel },
                navigate = { navController.navigate(Screen.HomeScreen) }
            )
        }
        composable<Screen.HomeScreen> {
            HomeScreenRoot(
                viewModel = viewModel { mainActivityComponent.homeScreenViewModel },
                navigate = { plant ->
                    navController.navigate(Screen.DetailScreen(plant.id))
                }
            )
        }
        composable<Screen.DetailScreen> {
            val args = it.toRoute<Screen.DetailScreen>()

            val viewModel = viewModel {
                mainActivityComponent.detailScreenViewModel(createSavedStateHandle())
            }

            viewModel.setPlantId(args.plantId)

            DetailScreenRoot(
                viewModel = viewModel,
                navigate = {
                    navController.navigate(Screen.HomeScreen)
                }
            )
        }
    }
}