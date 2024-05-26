package com.marmatsan.dev.watermyplants.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marmatsan.dev.catalog_ui.detail_screen.DetailScreen
import com.marmatsan.dev.catalog_ui.home_screen.HomeScreenRoot
import com.marmatsan.dev.catalog_ui.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.welcome_screen.WelcomeScreenRoot
import com.marmatsan.dev.watermyplants.MainActivityComponent
import com.marmatsan.dev.watermyplants.create
import com.marmatsan.dev.watermyplants.di.applicationComponent

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.DetailScreen
) {
    val mainActivityComponent =
        MainActivityComponent::class.create(LocalContext.current.applicationComponent)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.WelcomeScreen> {
            WelcomeScreenRoot(
                navigate = { navController.navigate(Screen.PlantScreen) },
                viewModel = viewModel { mainActivityComponent.welcomeScreenViewModel }
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
                viewModel = viewModel{ mainActivityComponent.homeScreenViewModel }
            )
        }
        composable<Screen.DetailScreen> {
            DetailScreen()
        }
    }
}