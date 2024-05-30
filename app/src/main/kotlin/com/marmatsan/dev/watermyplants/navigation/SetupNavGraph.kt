package com.marmatsan.dev.watermyplants.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenRoot
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenRoot
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.screen.welcome_screen.WelcomeScreenRoot
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
            WelcomeScreenRoot(
                viewModel = viewModel { mainActivityComponent.welcomeScreenViewModel },
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
                viewModel = viewModel
            )
        }
    }
}