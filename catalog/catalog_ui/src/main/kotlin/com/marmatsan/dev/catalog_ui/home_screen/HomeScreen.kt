package com.marmatsan.dev.catalog_ui.home_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.catalog_ui.home_screen.components.HomeScreenHeader
import com.marmatsan.dev.core_ui.components.illustration.Design
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeScreenState
) {
    Box(
        modifier = modifier
    ) {
        Illustration(
            modifier = Modifier.height(329.dp),
            design = Design.Two
        )
        Column(
            modifier = Modifier.padding(all = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeScreenHeader(
                query = "",
                onQueryChange = {},
                onSearch = {}
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    WaterMyPlantsTheme {
        HomeScreen(
            modifier = Modifier.size(
                width = 484.dp,
                height = 988.dp
            ),
            state = HomeScreenState(plants = emptyList()))
    }
}