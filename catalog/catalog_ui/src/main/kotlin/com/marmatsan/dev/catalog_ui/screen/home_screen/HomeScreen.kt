package com.marmatsan.dev.catalog_ui.screen.home_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_ui.screen.home_screen.components.HomeScreenHeader
import com.marmatsan.dev.catalog_ui.screen.home_screen.components.HomeScreenTabs
import com.marmatsan.dev.catalog_ui.screen.home_screen.components.PlantCard
import com.marmatsan.dev.catalog_ui.screen.home_screen.components.PlantCardDefaults
import com.marmatsan.dev.core_domain.isNull
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.extension.fillAvailableSpace
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding

@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel,
    navigate: (Plant) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        state = state,
        navigate = navigate
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeScreenState = HomeScreenState(),
    navigate: (Plant) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                },
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier.padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Illustration(
                illustrationDesign = IllustrationDesign.Two
            )
            Column(
                modifier = Modifier.padding(
                    horizontal = padding.none,
                    vertical = padding.medium
                ),
                verticalArrangement = Arrangement.spacedBy(padding.none, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HomeScreenHeader(
                    modifier = Modifier.padding(
                        horizontal = padding.medium
                    ),
                    query = "",
                    onQueryChange = {},
                    onSearch = {}
                )
                HomeScreenTabs(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            bottom = padding.small
                        )
                )
                if (state.plants.isNull()) {
                    Box(
                        modifier = Modifier.fillAvailableSpace(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "There are no plants :(")
                    }
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier,
                        contentPadding = PaddingValues(
                            horizontal = padding.medium,
                            vertical = padding.none
                        ),
                        columns = GridCells.Adaptive(minSize = PlantCardDefaults.minWidth),
                        horizontalArrangement = Arrangement.spacedBy(padding.medium),
                        verticalArrangement = Arrangement.spacedBy(padding.medium)
                    ) {
                        items(state.plants) { plant ->
                            PlantCard(
                                plant = plant,
                                onClick = {
                                    navigate(plant)
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Preview(
    widthDp = 484,
    heightDp = 988
)
@Composable
private fun HomeScreenPreview() {
    WaterMyPlantsTheme {
        HomeScreen(
            state = HomeScreenState().copy(
                plants = listOf(
                    Plant(
                        name = "Monstera",
                        shortDescription = "From Mexico",
                    ),
                    Plant(
                        name = "Cactus",
                        shortDescription = "Gift from mom",
                    ),
                    Plant(
                        name = "Bonsai",
                        shortDescription = "Care frequently",
                    ),
                    Plant(
                        name = "White peace lily",
                        shortDescription = "Planted on July",
                    ),
                    Plant(
                        name = "Jade",
                        shortDescription = "Needs a lot of sun",
                    ),
                    Plant(
                        name = "Rubber fig",
                        shortDescription = "From the garden",
                    )
                )
            ),
            navigate = {}
        )
    }
}