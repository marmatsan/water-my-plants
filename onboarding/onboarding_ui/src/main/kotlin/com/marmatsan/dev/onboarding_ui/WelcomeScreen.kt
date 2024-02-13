package com.marmatsan.dev.onboarding_ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.marmatsan.core_ui.iconadd.IconAdd
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.dimensions.theme.onBackgroundVariant
import com.marmatsan.onboarding_ui.R

@Composable
fun WelcomeScreen(
    onCreatePlantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(all = spacing.default),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = spacing.default),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.illustration_2),
                contentDescription = ""
            )
            Text(
                text = "Welcome to Water My Plants!",
                style = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center)
            )
        }
        // Body
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Welcome animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    (-38).dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.plant_1),
                    contentDescription = ""
                )
                Image(
                    painter = painterResource(id = R.drawable.plant_2),
                    contentDescription = ""
                )
                Image(
                    painter = painterResource(id = R.drawable.plant_3),
                    contentDescription = ""
                )
            }
            // Welcome text
            Column(
                modifier = modifier
                    .padding(
                        horizontal = spacing.small,
                        vertical = spacing.large
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    spacing.small,
                    Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Start your journey",
                    style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                )
                Text(
                    text = "There are no plants in the list, please add your first plant",
                    style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    color = MaterialTheme.colorScheme.onBackgroundVariant
                )
            }
            // Button container
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    spacing.small,
                    Alignment.Top
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    modifier = modifier.height(ButtonDefaults.MinHeight + density.positiveTwo),
                    onClick = {

                    }
                ) {
                    IconAdd(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .width(IntrinsicSize.Min)
                    )
                    Text(
                        text = "Add my first plant",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onCreatePlantClick = { /*TODO*/ })
}