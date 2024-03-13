package com.marmatsan.dev.onboarding_ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.core_ui.button.ButtonStyle
import com.marmatsan.core_ui.illustration.Illustration
import com.marmatsan.dev.core_ui.components.custom.button.Button
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.onBackgroundVariant
import com.marmatsan.onboarding_ui.R

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit
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
            modifier = modifier
                .fillMaxSize()
                .weight(0.33f)
        )
        Body(
            modifier = modifier
                .fillMaxSize()
                .weight(0.66f)
        )
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                all = spacing.default
            ),
        contentAlignment = Alignment.Center
    ) {
        Illustration(
            number = Number.Value2
        )
        // Header text
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.small,
                    end = spacing.small
                ),
            text = stringResource(id = R.string.welcome_screen_header),
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
            .padding(
                all = spacing.default
            ),
        verticalArrangement = Arrangement.spacedBy(
            space = spacing.default,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Illustration(
            number = Number.Value1,
            padding = PaddingValues(
                horizontal = spacing.large,
                vertical = spacing.large
            )
        )
        Content(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = spacing.large,
                    vertical = spacing.default
                )
        )
    }
}

@Composable
fun Content(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.large, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Texts(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    all = spacing.default
                )
        )
        Button(
            modifier = Modifier.height(ButtonDefaults.MinHeight + density.positiveTwo),
            buttonStyle = ButtonStyle.Filled,
            labelText = stringResource(id = R.string.welcome_screen_button_create_plant),
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(id = com.marmatsan.core_domain.R.string.cd_button_icon),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            onClick = {

            }
        )
    }
}

@Composable
fun Texts(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            spacing.small,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.welcome_screen_title_1),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.welcome_screen_title_2),
            color = MaterialTheme.colorScheme.onBackgroundVariant,
            style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
        )
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WaterMyPlantsTheme {
        WelcomeScreen(onCreatePlantClick = { })
    }
}