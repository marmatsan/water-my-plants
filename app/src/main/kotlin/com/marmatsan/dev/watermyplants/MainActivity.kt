package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenViewModel
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
import com.marmatsan.dev.watermyplants.di.applicationComponent
import com.marmatsan.dev.watermyplants.navigation.SetupNavGraph
import com.marmatsan.dev.watermyplants.splashscreen.SplashScreenViewModel
import com.marmatsan.dev.watermyplants.splashscreen.SplashScreenViewModelFactory
import me.tatarka.inject.annotations.Component

@Component
abstract class MainActivityComponent(
    @Component val parent: ApplicationComponent
) {
    abstract val splashScreenViewModel: SplashScreenViewModel
    abstract val plantScreenViewModel: (SavedStateHandle) -> PlantScreenViewModel
    abstract val homeScreenViewModel: HomeScreenViewModel
    abstract val detailScreenViewModel: (SavedStateHandle) -> DetailScreenViewModel
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainActivityComponent = MainActivityComponent::class.create(
            parent = applicationContext.applicationComponent
        )

        val factory = SplashScreenViewModelFactory(mainActivityComponent.splashScreenViewModel)

        val splashScreenViewModel = ViewModelProvider(
            owner = this,
            factory = factory
        )[SplashScreenViewModel::class.java]

        installSplashScreen().apply {
            setKeepOnScreenCondition { !splashScreenViewModel.state.value.isReady }
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
                val splashScreenViewModelState by splashScreenViewModel.state.collectAsStateWithLifecycle()
                val startDestination = splashScreenViewModelState.startDestination

                SetupNavGraph(
                    startDestination = startDestination,
                    mainActivityComponent = mainActivityComponent
                )
            }
        }
    }
}