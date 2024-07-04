package com.marmatsan.dev.catalog_ui.screen.welcome_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.density
import com.marmatsan.dev.core_ui.theme.onBackgroundVariant
import com.marmatsan.dev.core_ui.theme.padding

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    navigate: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(
                all = padding.none
            ),
        verticalArrangement = Arrangement.spacedBy(
            space = padding.none,
            alignment = Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            modifier = modifier
                .weight(0.33f)
                .padding(
                    all = padding.none
                )
        )
        Body(
            modifier = modifier
                .weight(0.66f),
            onCreatePlantClick = navigate
        )
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Illustration(
            illustrationDesign = IllustrationDesign.Two
        )
        // Header text
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = padding.large
                ),
            text = stringResource(id = R.string.welcome_screen_header),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center)
        )
        Illustration(
            illustrationDesign = IllustrationDesign.Three
        )
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(
                all = padding.none
            ),
        verticalArrangement = Arrangement.spacedBy(
            space = padding.none,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Illustration(
            modifier = Modifier.padding(
                start = padding.large,
                top = padding.extraLarge,
                end = padding.large,
                bottom = padding.none
            ),
            illustrationDesign = IllustrationDesign.One,
        )
        Content(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = padding.large,
                    vertical = padding.none
                ),
            onCreatePlantClick = onCreatePlantClick
        )
    }
}

@Composable
fun Content(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(padding.large, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Texts(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    all = padding.none
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
            onClick = onCreatePlantClick
        )
    }
}

@Composable
fun Texts(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            padding.small,
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
        WelcomeScreen(
            navigate = {}
        )
    }
}