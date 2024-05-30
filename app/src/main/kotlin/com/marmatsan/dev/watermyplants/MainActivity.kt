package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.welcome_screen.WelcomeScreenViewModel
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.navigation.SetupNavGraph
import me.tatarka.inject.annotations.Component

@Component
abstract class MainActivityComponent(@Component val parent: ApplicationComponent) {
    abstract val welcomeScreenViewModel: WelcomeScreenViewModel
    abstract val plantScreenViewModel: PlantScreenViewModel
    abstract val homeScreenViewModel: HomeScreenViewModel
    abstract val detailScreenViewModel: DetailScreenViewModel
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
                // val startDestination by splashScreenViewModel.isReady.collectAsStateWithLifecycle()
                SetupNavGraph(
                    navController = rememberNavController()
                )
            }
        }
    }
}