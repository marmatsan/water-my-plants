package com.marmatsan.dev.catalog_ui.plant_screen.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.density
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun PlantScreenActions(
    modifier: Modifier = Modifier,
    AIButtonAvailable: Boolean = false,
    image: Uri? = null,
    onAddImage: ((Uri) -> Unit)? = null,
    onAIButtonClick: (() -> Unit)? = null // TODO
) {
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { imageUri ->
                onAddImage?.invoke(imageUri)
            }
        }
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            spacing.small,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            modifier = Modifier.height(ButtonDefaults.MinHeight + density.positiveTwo),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ),
            labelText = if (image == null) {
                stringResource(id = R.string.plant_screen_button_add_image)
            } else {
                stringResource(id = R.string.plant_screen_button_change_image)
            },
            icon = {
                if (image == null) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.ChangeCircle,
                        contentDescription = null
                    )
                }
            },
            onClick = {
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
        if (AIButtonAvailable) {
            IconButton(
                modifier = Modifier.size(48.dp),
                iconButtonStyle = IconButtonStyle.Filled,
                iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colorScheme.primary
                ),
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(com.marmatsan.core_ui.R.drawable.icon_filled_sparkle),
                        tint = colorScheme.onPrimary,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun PlantScreenActionsPreview() {
    WaterMyPlantsTheme {
        PlantScreenActions(
            AIButtonAvailable = true
        )
    }
}