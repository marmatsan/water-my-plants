package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.onboarding_ui.screens.WelcomeScreen

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
                WelcomeScreen(onCreatePlantClick = { /*TODO*/ })
            }
        }
    }
}