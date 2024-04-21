package com.marmatsan.dev.catalog_ui.home_screen.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marmatsan.core_ui.R
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun PlantCard(
    modifier: Modifier = Modifier,
    waterQuantity: String? = null,
    wateringDay: String? = null,
    image: Uri? = null,
    name: String,
    shortDescription: String
) {
    Card(
        modifier = modifier.defaultMinSize(
            minWidth = PlantCardDefaults.minWidth,
            minHeight = PlantCardDefaults.minHeight,
        ),
        shape = shapes.medium,
        colors = CardColors(
            containerColor = colorScheme.surfaceContainerLow,
            contentColor = CardDefaults.cardColors().contentColor,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        )
    ) {
        PlantCardImage(
            modifier = Modifier.fillMaxWidth(),
            image = image
        )
        Container(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            name = name,
            shortDescription = shortDescription
        )
    }
}

@Composable
private fun PlantCardImage(
    modifier: Modifier = Modifier,
    image: Uri?
) {
    if (image != null) {
        CoilImage(
            modifier = modifier,
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            ),
            imageModel = { image }
        )
    } else {
        Image(
            modifier = modifier,
            alignment = Alignment.Center,
            painter = painterResource(id = R.drawable.plant_3),
            contentScale = ContentScale.None,
            contentDescription = null
        )
    }
}

@Composable
private fun Container(
    modifier: Modifier = Modifier,
    name: String,
    shortDescription: String
) {
    Row(
        modifier = modifier
            .padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.small,
                bottom = spacing.default
            ),
        horizontalArrangement = Arrangement.spacedBy(
            spacing.small,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Info
        Column(
            modifier = Modifier.padding(all = spacing.default),
            verticalArrangement = Arrangement.spacedBy(
                spacing.default,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = name,
                color = colorScheme.onSurface,
                style = typography.titleSmall
            )
            Text(
                text = shortDescription,
                color = colorScheme.onSurfaceVariant,
                style = typography.bodyMedium
            )
        }
        IconButton(
            modifier = Modifier.clip(shapes.small),
            shape = shapes.small,
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.iconButtonColors().copy(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            ),
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null
                )
            }
        )
    }
}

object PlantCardDefaults {
    val minWidth: Dp = 168.dp
    val minHeight: Dp = 268.dp
}

@Preview
@Composable
fun PlantCardPreview() {
    WaterMyPlantsTheme {
        PlantCard(
            modifier = Modifier.size(
                width = PlantCardDefaults.minWidth,
                height = PlantCardDefaults.minHeight,
            ),
            name = "Monstera",
            shortDescription = "Monstera plant"
        )
    }
}