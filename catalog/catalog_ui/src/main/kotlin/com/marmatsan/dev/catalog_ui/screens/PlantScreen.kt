package com.marmatsan.dev.catalog_ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.catalog_ui.R
import com.marmatsan.core_ui.button.ButtonStyle
import com.marmatsan.core_ui.illustration.Illustration
import com.marmatsan.core_ui.illustration.Number
import com.marmatsan.core_ui.plantscreenactions.PlantScreenActions
import com.marmatsan.core_ui.plantscreenheader.PlantScreenHeader
import com.marmatsan.dev.core_ui.components.relay.RelayButton
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.LocalElevation
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit
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
        Illustration(
            number = Number.Value3
        )
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
        PlantScreenHeader(
            modifier = Modifier.fillMaxWidth(),
            backIconButtonContainerColor = MaterialTheme.colorScheme.primary,
            deleteImageIconButtonContainerColor = MaterialTheme.colorScheme.secondary,
            backIconButtonIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = ""
                )
            },
            deleteImageIconButtonIcon = {
                Icon(
                    imageVector = Icons.Outlined.ImageNotSupported,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = ""
                )
            }
        )
        Image(
            painter = painterResource(id = R.drawable.plant_icon),
            contentDescription = ""
        )
        PlantScreenActions(
            modifier = Modifier.height(IntrinsicSize.Min),
            showAIAssistant = true,
            showIcon = true,
            mainButtonLabel = "Add plant"
        )
    }
}

@Composable
fun Form(
    modifier: Modifier = Modifier
) {

    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier
            .fillMaxSize(),
        shadowElevation = LocalElevation.current.level3
    ) {
        Column(
            modifier = modifier.padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.medium,
                bottom = spacing.default
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {

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
        RelayButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonDefaults.MinHeight + 16.dp),
            buttonStyle = ButtonStyle.Outlined,
            label = "Create plant",
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