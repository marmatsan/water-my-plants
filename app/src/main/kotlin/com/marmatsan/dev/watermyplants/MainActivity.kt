package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenRoot
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenViewModel
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.di.applicationComponent
import me.tatarka.inject.annotations.Component

@Component
abstract class MainActivityComponent(@Component val parent: ApplicationComponent) {
    abstract val plantScreenViewModel: PlantScreenViewModel
    abstract val homeScreenViewModel: HomeScreenViewModel
    abstract val detailScreenViewModel: (SavedStateHandle) -> DetailScreenViewModel
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
                val mainActivityComponent =
                    MainActivityComponent::class.create(LocalContext.current.applicationComponent)

                PlantScreenRoot(
                    viewModel = viewModel { mainActivityComponent.plantScreenViewModel },
                    navigate = {}
                )
            }
        }
    }
}