package com.marmatsan.dev.catalog_ui.plant_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.catalog_ui.plant_screen.components.PlantScreenActions
import com.marmatsan.dev.catalog_ui.plant_screen.components.PlantScreenForm
import com.marmatsan.dev.catalog_ui.plant_screen.components.PlantScreenHeader
import com.marmatsan.dev.catalog_ui.plant_screen.components.PlantSizeDialog
import com.marmatsan.dev.catalog_ui.plant_screen.components.PlantWateringDaysDialog
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.illustration.Design
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.event.ObserveAsEvents
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    state: PlantScreenState,
    onAction: (PlantScreenAction) -> Unit,
    UIEventFlow: Flow<PlantScreenEvent>,
) {
    val spacing = LocalSpacing.current

    ObserveAsEvents(flow = UIEventFlow) { event ->
        when (event) { // TODO
            else -> {}
        }
    }

    if (state.wateringDaysDialogVisible) {
        PlantWateringDaysDialog(
            wateringDays = state.plant.wateringDays,
            onCancelWateringDaysDialog = {
                onAction(PlantScreenAction.OnDismissWateringDaysDialog)
            },
            onConfirmWateringDaysDialog = { newWateringDays ->
                onAction(PlantScreenAction.OnWateringDaysChange(newWateringDays))
                onAction(PlantScreenAction.OnDismissWateringDaysDialog)
            }
        )
    }

    if (state.plantSizeDialogVisible) {
        PlantSizeDialog(
            plantSize = state.plant.size,
            onCancelPlantSizeDialog = {
                onAction(PlantScreenAction.OnDismissPlantSizeDialog)
            },
            onConfirmPlantSizeDialog = { selectedPlantSize ->
                onAction(PlantScreenAction.OnSizeChange(selectedPlantSize)) // TODO: Check
                onAction(PlantScreenAction.OnDismissPlantSizeDialog)
            }
        )
    }
    
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
                ),
            removePhotoAvailable = state.removePhotoAvailable,
            AIButtonAvailable = state.AIButtonAvailable
        )
        PlantScreenForm(
            modifier = modifier
                .fillMaxSize()
                .weight(5f),
            name = state.plant.name,
            wateringDays = state.plant.wateringDays,
            waterAmount = state.plant.waterAmount,
            plantSize = state.plant.size,
            onPlantSizeClick = {
                onAction(PlantScreenAction.OnPlantSizeClick)
            },
            onWateringDaysClick = {
                onAction(PlantScreenAction.OnWateringDaysClick)
            },
            onWateringTimeClick = {
                onAction(PlantScreenAction.OnWateringTimeClick)
            },
            onWaterAmountChange = { newWaterAmount ->
                onAction(PlantScreenAction.OnWaterAmountChange(newWaterAmount.toInt()))
            }
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
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    AIButtonAvailable: Boolean = false
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
    ) {
        Illustration(
            modifier = Modifier.fillMaxSize(),
            design = Design.Three
        )
        HeaderContent(
            modifier = modifier.padding(
                all = spacing.medium
            ),
            removePhotoAvailable = removePhotoAvailable,
            AIButtonAvailable = AIButtonAvailable
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    AIButtonAvailable: Boolean = false
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlantScreenHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = spacing.default),
            removePhotoAvailable = removePhotoAvailable
        )
        Image( // Plant icon
            painter = painterResource(id = com.marmatsan.core_ui.R.drawable.plant_icon),
            contentDescription = null
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = spacing.default),
            AIButtonAvailable = AIButtonAvailable
        )
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
                .height(ButtonDefaults.MinHeight + LocalDensity.current.positiveFour),
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
        PlantScreen(
            state = PlantScreenState(),
            onAction = {},
            UIEventFlow = flow { }
        )
    }
}