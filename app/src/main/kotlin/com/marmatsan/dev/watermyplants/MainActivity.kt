package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marmatsan.dev.catalog_ui.plant_screen.PlantScreen
import com.marmatsan.dev.catalog_ui.plant_screen.PlantScreenViewModel
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.di.applicationComponent
import me.tatarka.inject.annotations.Component

@Component
abstract class MainActivityComponent(@Component val parent: ApplicationComponent) {
    abstract val plantScreenViewModel: PlantScreenViewModel
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreenViewModel by viewModels<SplashScreenViewModel>()

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !splashScreenViewModel.isReady.value
            }

            setOnExitAnimationListener { splashScreenViewProvider ->

                val iconFadeOut = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.view,
                    View.ALPHA,
                    0f,
                    0f
                )

                iconFadeOut.apply {
                    interpolator = AnticipateInterpolator()
                    duration = 50L
                }

                val screenFadeOut = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.view,
                    View.ALPHA,
                    1f,
                    0f
                )

                screenFadeOut.apply {
                    interpolator = AnticipateInterpolator()
                    duration = 300L
                    doOnEnd { splashScreenViewProvider.remove() }
                }
                iconFadeOut.start()
                screenFadeOut.start()
            }
        }

        setContent {
            WaterMyPlantsTheme {
                // val startDestination by splashScreenViewModel.startDestination.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                /* Scaffold(
                     modifier = Modifier.fillMaxSize(),
                     snackbarHost = { SnackbarHost(snackbarHostState) }
                 ) { paddingValues ->
                     *//* val navController = rememberNavController()
                     SetupNavGraph(
                         navController = navController,
                         startDestination = startDestination,
                         paddingValues = paddingValues,
                         snackbarHostState = snackbarHostState
                     )*//*
                }*/
                val mainActivityComponent =
                    MainActivityComponent::class.create(applicationComponent)

                val plantScreenViewModel = viewModel { mainActivityComponent.plantScreenViewModel }
                val plantScreenState by plantScreenViewModel.state.collectAsStateWithLifecycle()

                PlantScreen(
                    state = plantScreenState,
                    onAction = plantScreenViewModel::onAction,
                    UIEventFlow = plantScreenViewModel.UIEventFlow
                )
            }
        }
    }
}