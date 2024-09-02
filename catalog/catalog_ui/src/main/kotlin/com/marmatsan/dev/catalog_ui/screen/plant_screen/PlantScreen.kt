package com.marmatsan.dev.catalog_ui.screen.plant_screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantScreenActions
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantScreenForm
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantSizeDialog
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantWateringDaysDialog
import com.marmatsan.dev.catalog_ui.screen.plant_screen.components.PlantWateringTimeDialog
import com.marmatsan.dev.core_domain.isNull
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.dialog.BasicDialog
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.components.twoiconbuttonsheader.TwoIconButtonsHeader
import com.marmatsan.dev.core_ui.event.ObserveAsEvents
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.density
import com.marmatsan.dev.core_ui.theme.padding
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.launch

private val LocalPlantId = compositionLocalOf<String?> { null }

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    viewModel: PlantScreenViewModel,
    navigate: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { imageUri ->
                viewModel.onAction(PlantScreenAction.OnImageChange(imageUri))
            }
        }
    )

    val requestMediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isPermissionGranted ->
            viewModel.sendEvent(
                PlantScreenEvent.PermissionResultChanged(
                    permission = Permission.READ_MEDIA_IMAGES,
                    isPermissionGranted = isPermissionGranted
                )
            )
        }
    )

    ObserveAsEvents(uiEventFlow = viewModel.uiEventFlow) { event ->
        when (event) {
            is PlantScreenEvent.PlantCreated, PlantScreenEvent.BackClicked -> {
                navigate()
            }

            is PlantScreenEvent.RequestPermission -> {
                val permissionRequested = when (event.permission) {
                    Permission.READ_MEDIA_IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
                }
                requestMediaPermissionLauncher.launch(permissionRequested)
            }

            is PlantScreenEvent.ShowExplanatorySnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.plant_screen_snackbar_message)
                    )
                }
            }

            is PlantScreenEvent.LaunchImagePicker -> {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }

            is PlantScreenEvent.PermissionResultChanged -> {
                viewModel.onPermissionResultChanged(
                    permission = event.permission,
                    isPermissionGranted = event.isPermissionGranted
                )
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalPlantId provides viewModel.plantId
    ) {
        PlantScreen(
            modifier = modifier,
            snackbarHostState = snackbarHostState,
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    state: PlantScreenState,
    onAction: (PlantScreenAction) -> Unit
) {
    if (state.isWateringDaysDialogVisible) {
        PlantWateringDaysDialog(
            wateringDays = state.plant.wateringDays,
            onCancelWateringDaysDialog = {
                onAction(PlantScreenAction.OnDismissWateringDaysDialog)
            },
            onConfirmWateringDaysDialog = { newWateringDays ->
                onAction(PlantScreenAction.OnWateringDaysChange(newWateringDays)) // TODO: Simplify into single action
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
                onAction(PlantScreenAction.OnWateringTimeChange(newWateringTime)) // TODO: Simplify into single action
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
                onAction(PlantScreenAction.OnSizeChange(selectedPlantSize)) // TODO: Simplify into single action
                onAction(PlantScreenAction.OnDismissPlantSizeDialog)
            }
        )
    }

    if (state.isMediaPermissionRationaleVisible) {
        BasicDialog(
            icon = Icons.Rounded.WarningAmber,
            headline = stringResource(R.string.plant_screen_dialog_rationale_headline),
            supportingText = stringResource(R.string.plant_screen_dialog_rationale_supporting_text),
            dismissRequestActionLabel = stringResource(R.string.plant_screen_dialog_rationale_action_2),
            onDismissRequest = {
                onAction(PlantScreenAction.OnRetryRequestPermission(Permission.READ_MEDIA_IMAGES))
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    color = colorScheme.background
                )
                .padding(
                    paddingValues = paddingValues
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            Header(
                modifier = Modifier.weight(1f),
                removePhotoAvailable = state.plant.image != null,
                aiButtonAvailable = false,
                image = state.plant.image,
                onAddImage = {
                    onAction(PlantScreenAction.OnAddOrChangeImage)
                },
                onBackClick = {
                    onAction(PlantScreenAction.OnBackClick)
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
                        horizontal = padding.medium,
                        vertical = padding.small
                    ),
                onCreatePlant = {
                    onAction(PlantScreenAction.OnCreatePlant)
                },
                createPlantButtonIsEnabled = state.isCreatePlantButtonEnabled
            )
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    image: Uri? = null,
    onAddImage: (() -> Unit),
    onBackClick: (() -> Unit)? = null,
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
                    all = padding.medium
                )
                .fillMaxSize(),
            removePhotoAvailable = removePhotoAvailable,
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
            onBackClick = onBackClick,
            onRemoveImage = onRemoveImage
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    onAddImage: (() -> Unit),
    image: Uri? = null,
    onBackClick: (() -> Unit)? = null,
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
                .padding(all = padding.none),
            showSecondaryButton = removePhotoAvailable,
            onPrimaryIconButtonClick = onBackClick,
            onSecondaryIconButtonClick = onRemoveImage
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = padding.none),
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
    val plantId = LocalPlantId.current

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            space = padding.none,
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
            labelText = stringResource(
                id = if (plantId.isNull())
                    R.string.plant_screen_button_create_plant
                else
                    R.string.plant_screen_button_update_plant
            ),
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (plantId.isNull())
                        Icons.Outlined.Add
                    else
                        Icons.Outlined.ChangeCircle,
                    contentDescription = null
                )
            },
            onClick = onCreatePlant
        )
    }
}

@Preview
@Composable
private fun PlantScreenPreview() {
    WaterMyPlantsTheme {
        PlantScreen(
            state = PlantScreenState(),
            snackbarHostState = SnackbarHostState(),
            onAction = {}
        )
    }
}