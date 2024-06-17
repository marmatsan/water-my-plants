package com.marmatsan.dev.watermyplants

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.SavedStateHandle
import com.marmatsan.dev.catalog_ui.screen.detail_screen.DetailScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.home_screen.HomeScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.plant_screen.PlantScreenViewModel
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantScreenForm
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.watermyplants.di.ApplicationComponent
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
                PlantScreenTest()
            }
        }
    }
}

@Composable
fun PlantScreenTest(
    modifier: Modifier = Modifier
) {

    var description by remember {
        mutableStateOf<String?>(LoremIpsum(words = 20).values.joinToString(separator = " "))
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.Black)
        ) {
            // Empty
        }
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            PlantScreenForm(
                description = description,
                onDescriptionChange = { newDescription ->
                    description = newDescription
                }
            )
        }
    }
}

@Preview
@Composable
private fun PlantScreenTestPreview() {
    WaterMyPlantsTheme {
        PlantScreenTest()
    }
}