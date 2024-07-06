package com.marmatsan.dev.watermyplants.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenRoot
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenEvent
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenRoot
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.screen.welcome_screen.WelcomeScreen
import com.marmatsan.dev.core_ui.screen.Screen
import com.marmatsan.dev.watermyplants.MainActivityComponent

@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.WelcomeScreen,
    mainActivityComponent: MainActivityComponent
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.WelcomeScreen> {
            WelcomeScreen(
                navigate = {
                    navController.navigate(Screen.PlantScreen())
                }
            )
        }
        composable<Screen.PlantScreen> {
            val args = it.toRoute<Screen.PlantScreen>()
            val viewModel = viewModel {
                mainActivityComponent.plantScreenViewModel(createSavedStateHandle())
            }
            viewModel.setPlantId(args.plantId)

            PlantScreenRoot(
                viewModel = viewModel,
                navigate = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.HomeScreen> {
            HomeScreenRoot(
                viewModel = viewModel { mainActivityComponent.homeScreenViewModel },
                onHomeScreenEvent = { event ->
                    when (event) {
                        is HomeScreenEvent.NavigateToPlantScreen ->
                            navController.navigate(Screen.PlantScreen())

                        is HomeScreenEvent.NavigateToDetailScreen -> {
                            navController.navigate(Screen.DetailScreen(event.plantId))
                        }
                    }
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
                navigate = { screen ->
                    navController.navigate(screen)
                }
            )
        }
    }
}