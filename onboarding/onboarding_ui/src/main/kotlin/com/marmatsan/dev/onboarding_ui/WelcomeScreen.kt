package com.marmatsan.dev.onboarding_ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.dimensions.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.dimensions.theme.onBackgroundVariant
import com.marmatsan.onboarding_ui.R

@Composable
fun WelcomeScreen(
    onCreatePlantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(
                all = LocalSpacing.current.default
            ),
        verticalArrangement = Arrangement.spacedBy(
            space = LocalSpacing.current.default,
            alignment = Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            modifier = modifier.padding(
                all = LocalSpacing.current.default
            )
        )
        Body(
            modifier = modifier.padding(
                    all = LocalSpacing.current.default
            )
        )
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Illustration=2
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.illustration_2_objects_left),
                contentDescription = ""
            )
            Image(
                painter = painterResource(id = R.drawable.illustration_2_objects_right),
                contentDescription = ""
            )
        }
        // Header text
        Text(
            text = "Welcome to Water My Plants!",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center)
        )
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = spacing.default),
        verticalArrangement = Arrangement.spacedBy(
            space = spacing.default,
            alignment = Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Welcome animation
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    top = spacing.large,
                    bottom = spacing.large
                ),
            horizontalArrangement = Arrangement.spacedBy(
                space = (-38).dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.illustration_1_plant_1),
                contentDescription = ""
            )
            Image(
                painter = painterResource(id = R.drawable.illustration_1_plant_2),
                contentDescription = ""
            )
            Image(
                painter = painterResource(id = R.drawable.illustration_1_plant_3),
                contentDescription = ""
            )
        }
        // Welcome text
        Column(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
                .padding(
                    horizontal = spacing.large,
                    vertical = spacing.default
                ),
            verticalArrangement = Arrangement.spacedBy(
                spacing.small,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = modifier.fillMaxWidth(),
                text = "Start your journey",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
            )
            Text(
                modifier = modifier.fillMaxWidth(),
                text = "There are no plants in the list, please add your first plant",
                style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onBackgroundVariant
            )
        }
        // Button container
        Column(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
                .padding(all = spacing.default),
            verticalArrangement = Arrangement.spacedBy(
                space = spacing.default,
                alignment = Alignment.Top
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                modifier = modifier.height(ButtonDefaults.MinHeight + LocalDensity.current.positiveTwo),
                onClick = {

                }
            ) {
                Text(
                    text = "Add my first plant",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WaterMyPlantsTheme {
        WelcomeScreen(onCreatePlantClick = { })
    }
}