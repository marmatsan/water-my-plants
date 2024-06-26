package com.marmatsan.dev.catalog_ui.screen.home_screen.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marmatsan.core_ui.R
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.extension.fillAvailableSpace
import com.marmatsan.dev.core_ui.extension.toFormattedString
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.time.DayOfWeek

object PlantCardDefaults {
    val minWidth: Dp = 168.dp
    val minHeight: Dp = 268.dp
    val maxWidth: Dp = 218.dp
    val maxHeight: Dp = 268.dp
}

@Composable
fun PlantCard(
    modifier: Modifier = Modifier,
    plant: Plant,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(shapes.medium)
            .defaultMinSize(
                minWidth = PlantCardDefaults.minWidth,
                minHeight = PlantCardDefaults.minHeight,
            )
            .clickable(
                onClick = onClick
            ),
        colors = CardColors(
            containerColor = colorScheme.surfaceContainerLow,
            contentColor = CardDefaults.cardColors().contentColor,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        )
    ) {
        PlantCardImage(
            modifier = Modifier.fillAvailableSpace(),
            image = plant.image,
            waterAmount = plant.waterAmount,
            wateringDays = plant.wateringDays
        )
        Container(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            name = plant.name,
            shortDescription = plant.shortDescription
        )
    }
}

@Composable
private fun PlantCardImage(
    modifier: Modifier = Modifier,
    image: Uri?,
    waterAmount: Int? = null,
    wateringDays: List<DayOfWeek>? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        if (image.isNotNull())
            CoilImage(
                modifier = Modifier.fillMaxSize(),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                imageModel = { image }
            )
        else
            Image(
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.plant_3_small),
                contentScale = ContentScale.None,
                contentDescription = null
            )

        // plant info
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(
                    top = 12.dp,
                    start = 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(
                space = spacing.small,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            if (waterAmount.isNotNull())
                Surface(
                    modifier = Modifier
                        .clip(shapes.extraSmall)
                        .alpha(0.56f)
                        .background(colorScheme.surface)
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                ) {
                    Text(
                        text = "$waterAmount ml",
                        style = typography.labelSmall,
                        color = colorScheme.onSurface
                    )
                }

            if (wateringDays.isNotNull())
                Surface(
                    modifier = Modifier
                        .clip(shapes.extraSmall)
                        .alpha(0.56f)
                        .background(colorScheme.surface)
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                        .wrapContentWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentWidth()
                            .widthIn(
                                max = 80.dp
                            )
                            .basicMarquee(),
                        text = wateringDays.toFormattedString(),
                        style = typography.labelSmall,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }
    }
}

@Composable
private fun Container(
    modifier: Modifier = Modifier,
    name: String?,
    shortDescription: String?
) {
    Row(
        modifier = modifier
            .padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.small,
                bottom = spacing.medium
            ),
        horizontalArrangement = Arrangement.spacedBy(
            spacing.small,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(all = spacing.none),
            verticalArrangement = Arrangement.spacedBy(
                spacing.none,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = name ?: String.Empty,
                color = colorScheme.onSurface,
                style = typography.titleSmall
            )
            if (shortDescription.isNotNull())
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = shortDescription,
                    color = colorScheme.onSurfaceVariant,
                    style = typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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

@Preview(
    widthDp = 168,
    heightDp = 268
)
@Composable
fun PlantCardWithoutPhotoPreview() {
    WaterMyPlantsTheme {
        PlantCard(
            plant = Plant(
                name = "Monstera",
                wateringDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                waterAmount = 50
            ),
            onClick = {}
        )
    }
}