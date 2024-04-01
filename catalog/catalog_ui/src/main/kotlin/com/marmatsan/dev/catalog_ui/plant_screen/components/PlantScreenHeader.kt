package com.marmatsan.dev.catalog_ui.plant_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun PlantScreenHeader(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    contentDescription = null
                )
            }

        )
        if (removePhotoAvailable) {
            IconButton(
                iconButtonStyle = IconButtonStyle.Filled,
                iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.HideImage,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        contentDescription = null
                    )
                }

            )
        }
    }
}

@Preview(
    widthDp = 484
)
@Composable
fun PlantScreenHeaderPreview() {
    WaterMyPlantsTheme {
        PlantScreenHeader(
            removePhotoAvailable = true
        )
    }
}