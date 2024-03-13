package com.marmatsan.dev.catalog_ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.components.custom.button.Button
import com.marmatsan.dev.core_ui.components.custom.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.custom.textfield.TextField
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.LocalElevation
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(
                all = spacing.default
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Header(
            modifier = modifier
                .fillMaxSize()
                .weight(5f)
                .padding(
                    all = spacing.default
                )
        )
        Form(
            modifier = modifier
                .fillMaxSize()
                .weight(5f)
        )
        ButtonContainer(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.background
                )
                .padding(
                    horizontal = spacing.medium,
                    vertical = spacing.small
                )
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
    ) {
        HeaderContent(
            modifier = modifier.padding(
                all = spacing.medium
            )
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

    }
}

@Composable
fun Form(
    modifier: Modifier = Modifier
) {

    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier.fillMaxSize(),
        shadowElevation = LocalElevation.current.level3
    ) {
        Column(
            modifier = Modifier.padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.medium,
                bottom = spacing.default
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .padding(all = spacing.default)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(
                    spacing.medium,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
/*                RelayTextField(
                    modifier = Modifier
                        .weight(5f)
                        .clickable { },
                    labelText = stringResource(id = R.string.plant_screen_text_field_label_watering_days),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderRadiusBottomEnd = ShapesDefaults.extraSmall.value.toDouble(),
                    borderRadiusBottomStart = ShapesDefaults.extraSmall.value.toDouble(),
                    showActiveIndicator = false,
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = ""
                        )
                    }
                )
                RelayTextField(
                    modifier = Modifier.weight(5f),
                    labelText = stringResource(id = R.string.plant_screen_text_field_label_watering_time),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderRadiusBottomEnd = ShapesDefaults.extraSmall.value.toDouble(),
                    borderRadiusBottomStart = ShapesDefaults.extraSmall.value.toDouble(),
                    showActiveIndicator = false,
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = ""
                        )
                    }
                )*/
            }
            Row(
                modifier = Modifier
                    .padding(all = spacing.default)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(
                    spacing.medium,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
/*                RelayTextField(
                    modifier = Modifier.weight(5f),
                    labelText = stringResource(id = R.string.plant_screen_text_field_label_water_amount),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderRadiusBottomEnd = ShapesDefaults.extraSmall.value.toDouble(),
                    borderRadiusBottomStart = ShapesDefaults.extraSmall.value.toDouble(),
                    showActiveIndicator = false
                )
                RelayTextField(
                    modifier = Modifier.weight(5f),
                    labelText = stringResource(id = R.string.plant_screen_text_field_label_plant_size),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderRadiusBottomEnd = ShapesDefaults.extraSmall.value.toDouble(),
                    borderRadiusBottomStart = ShapesDefaults.extraSmall.value.toDouble(),
                    showActiveIndicator = false,
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = ""
                        )
                    }
                )*/
            }
/*            RelayTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                labelText = stringResource(id = R.string.plant_screen_text_field_label_description),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                borderRadiusBottomEnd = ShapesDefaults.extraSmall.value.toDouble(),
                borderRadiusBottomStart = ShapesDefaults.extraSmall.value.toDouble(),
                showActiveIndicator = false
            )*/
        }
    }
}

@Composable
fun ButtonContainer(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonDefaults.MinHeight + 16.dp),
            buttonStyle = ButtonStyle.Outlined,
            labelText = "Create plant",
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = ""
                )
            }
        )
    }
}

@Preview
@Composable
fun PlantScreenPreview() {
    WaterMyPlantsTheme {
        PlantScreen {

        }
    }
}