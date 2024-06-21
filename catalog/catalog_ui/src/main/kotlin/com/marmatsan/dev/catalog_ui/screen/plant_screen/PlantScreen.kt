package com.marmatsan.dev.catalog_ui.screen.plant_screen

import android.net.Uri
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantScreenActions
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantScreenForm
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantSizeDialog
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantWateringDaysDialog
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantWateringTimeDialog
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.components.twoiconbuttonsheader.TwoIconButtonsHeader
import com.marmatsan.dev.core_ui.event.ObserveAsEvents
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.density
import com.marmatsan.dev.core_ui.theme.spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun PlantScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: PlantScreenViewModel,
    navigate: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction
    val uiEventFlow = viewModel.uiEventFlow

    ObserveAsEvents(uiEventFlow = uiEventFlow) { event ->
        // TODO
    }

    PlantScreen(
        modifier = modifier,
        state = state,
        onAction = onAction,
        navigate = navigate
    )
}

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    state: PlantScreenState,
    onAction: (PlantScreenAction) -> Unit,
    navigate: () -> Unit
) {
    if (state.isWateringDaysDialogVisible) {
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

    if (state.isWateringTimeDialogVisible) {
        PlantWateringTimeDialog(
            wateringTime = state.plant.wateringTime,
            onCancelWateringTimeDialog = {
                onAction(PlantScreenAction.OnDismissWateringTimeDialog)
            },
            onConfirmWateringTimeDialog = { newWateringTime ->
                onAction(PlantScreenAction.OnWateringTimeChange(newWateringTime))
                onAction(PlantScreenAction.OnDismissWateringTimeDialog)
            }
        )
    }

    if (state.isPlantSizeDialogVisible) {
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
                color = colorScheme.background
            )
            .padding(
                all = spacing.default
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start,
    ) {
        Header(
            modifier = Modifier.weight(1f),
            removePhotoAvailable = state.plant.image != null,
            aiButtonAvailable = false,
            image = state.plant.image,
            onAddImage = { imageUri ->
                onAction(PlantScreenAction.OnAddImage(imageUri))
            },
            onRemoveImage = {
                onAction(PlantScreenAction.OnRemoveImage)
            }
        )
        PlantScreenForm(
            modifier = Modifier.weight(1f),
            name = state.plant.name,
            wateringDays = state.plant.wateringDays,
            wateringTime = state.plant.wateringTime,
            waterAmount = state.plant.waterAmount,
            plantSize = state.plant.size,
            description = state.plant.description,
            onNameChange = { newName ->
                onAction(PlantScreenAction.OnPlantNameChange(newName))
            },
            onWateringDaysClick = {
                onAction(PlantScreenAction.OnWateringDaysClick)
            },
            onWateringTimeClick = {
                onAction(PlantScreenAction.OnWateringTimeClick)
            },
            onWaterAmountChange = { newWaterAmount ->
                onAction(PlantScreenAction.OnWaterAmountChange(newWaterAmount))
            },
            onPlantSizeClick = {
                onAction(PlantScreenAction.OnPlantSizeClick)
            },
            onDescriptionChange = { newDescription ->
                onAction(PlantScreenAction.OnDescriptionChange(newDescription))
            }
        )
        ButtonContainer(
            modifier = Modifier
                .wrapContentHeight()
                .background(
                    color = colorScheme.background
                )
                .padding(
                    horizontal = spacing.medium,
                    vertical = spacing.small
                ),
            onCreatePlant = {
                onAction(PlantScreenAction.OnCreatePlant)
                navigate()
            },
            createPlantButtonIsEnabled = state.isCreatePlantButtonEnabled
        )
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    image: Uri? = null,
    onAddImage: ((Uri) -> Unit)? = null,
    onRemoveImage: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        image?.let {
            CoilImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { image },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
        } ?: Illustration(
            modifier = Modifier.fillMaxSize(),
            illustrationDesign = IllustrationDesign.Four
        )
        HeaderContent(
            modifier = Modifier
                .padding(
                    all = spacing.medium
                )
                .fillMaxSize(),
            removePhotoAvailable = removePhotoAvailable,
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
            onRemoveImage = onRemoveImage
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    onAddImage: ((Uri) -> Unit)? = null,
    image: Uri? = null,
    onRemoveImage: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TwoIconButtonsHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = spacing.default),
            showSecondaryButton = removePhotoAvailable,
            onSecondaryIconButtonClick = onRemoveImage
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = spacing.default),
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
        )
    }
}

@Composable
fun ButtonContainer(
    modifier: Modifier = Modifier,
    createPlantButtonIsEnabled: Boolean,
    onCreatePlant: () -> Unit
) {
    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            space = spacing.default,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonDefaults.MinHeight + density.positiveFour),
            enabled = createPlantButtonIsEnabled,
            buttonStyle = ButtonStyle.Outlined,
            labelText = "Create plant",
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    contentDescription = ""
                )
            },
            onClick = onCreatePlant
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
            navigate = {}
        )
    }
}