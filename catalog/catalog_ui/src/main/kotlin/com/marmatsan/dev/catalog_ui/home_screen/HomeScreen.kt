package com.marmatsan.dev.catalog_ui.home_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_ui.home_screen.components.HomeScreenHeader
import com.marmatsan.dev.catalog_ui.home_screen.components.HomeScreenTabs
import com.marmatsan.dev.catalog_ui.home_screen.components.PlantCard
import com.marmatsan.dev.catalog_ui.home_screen.components.PlantCardDefaults
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeScreenState = HomeScreenState()
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
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
            modifier = modifier.padding(paddingValues)
        ) {
            Illustration(
                modifier = Modifier.height(329.dp),
                illustrationDesign = IllustrationDesign.Two
            )
            Column(
                modifier = Modifier.padding(
                    horizontal = spacing.default,
                    vertical = spacing.medium
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HomeScreenHeader(
                    modifier = Modifier.padding(
                        horizontal = spacing.medium
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
                            bottom = spacing.small
                        )
                )
                LazyVerticalGrid(
                    modifier = Modifier,
                    contentPadding = PaddingValues(
                        horizontal = spacing.medium,
                        vertical = spacing.default
                    ),
                    columns = GridCells.Adaptive(minSize = PlantCardDefaults.minWidth),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    items(state.plants ?: emptyList()) { plant ->
                        PlantCard(
                            name = plant.name,
                            shortDescription = plant.shortDescription
                        )
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
            )
        )
    }
}