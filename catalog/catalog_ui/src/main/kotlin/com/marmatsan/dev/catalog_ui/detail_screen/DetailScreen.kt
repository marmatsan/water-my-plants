package com.marmatsan.dev.catalog_ui.detail_screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.core_ui.R
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.components.twoiconbuttonsheader.TwoIconButtonsHeader
import com.marmatsan.dev.core_ui.theme.ShapeDefaults
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    state: DetailScreenState = DetailScreenState()
) {
    Column(
        modifier = modifier
            .background(
                color = colorScheme.background
            )
            .padding(
                start = spacing.default,
                top = spacing.default,
                end = spacing.default,
                bottom = spacing.small
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlantDetails(
            plantName = state.plant.name ?: String.Empty,
            plantDescription = state.plant.description ?: String.Empty
        )

    }
}

@Composable
private fun PlantDetails(
    modifier: Modifier = Modifier,
    plantName: String,
    plantDescription: String
) {
    Column(
        modifier = modifier
            .padding(
                all = spacing.default
            ),
        verticalArrangement = Arrangement.spacedBy((-28).dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        Header(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        )
        PlantInfo(
            modifier = modifier.weight(1f),
            plantName = plantName,
            plantDescription = plantDescription
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,

    ) {
    Box(
        modifier = modifier
    ) {
        HeaderBackground()
        Column(
            modifier = modifier.padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.medium,
                bottom = 56.dp
            ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TwoIconButtonsHeader(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HeaderBackground(
    modifier: Modifier = Modifier,
    image: Uri? = null
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
                modifier = Modifier
                    .background(colorScheme.surfaceContainer)
                    .fillMaxSize(),
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.plant_3_small),
                contentScale = ContentScale.Fit,
                contentDescription = null
            )
    }
}

@Composable
private fun PlantInfo(
    modifier: Modifier = Modifier,
    plantName: String,
    plantDescription: String
) {

    @Composable
    fun PlantDescription() {
        Text(
            text = plantDescription,
            style = typography.bodyLarge
        )
    }

    Column(
        modifier = modifier
            .background(
                color = colorScheme.surface,
                shape = RoundedCornerShape(
                    topStart = ShapeDefaults.extraLarge,
                    topEnd = ShapeDefaults.extraLarge,
                    bottomStart = ShapeDefaults.none,
                    bottomEnd = ShapeDefaults.none
                )
            )
            .padding(
                start = spacing.large,
                top = spacing.large,
                end = spacing.large,
                bottom = spacing.medium
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.semiLarge, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = plantName,
            style = typography.headlineLarge
        )
        PlantDescription()
    }
}

@Preview
@Composable
private fun DetailScreenPreview() {
    WaterMyPlantsTheme {
        DetailScreen(
            state = DetailScreenState().copy(
                plant = Plant(
                    name = "Monstera",
                    description = "The Monstera plant, scientifically known as Monstera deliciosa, is a popular tropical houseplant with distinctive, broad, glossy leaves that are often full of natural holes, giving it a unique and attractive appearance. Here are some key features and care tips for the Monstera plant:\u2028\u2028Foliage: Monstera plants are known for their large, fenestrated leaves. The fenestrations, or natural holes, in the leaves contribute to their iconic look. As the plant matures, these holes become more pronounced.\u2028\u2028Growth: Monstera plants are climbers by nature. In their natural habitat, they use aerial roots to climb up trees. Indoors, they can be trained to climb a support or left to cascade as a hanging plant.\u2028\u2028Light: They thrive in bright, indirect light. While they can tolerate lower light conditions, they may not grow as vigorously. Avoid direct sunlight, as it can scorch the leaves.\u2028\u2028Watering: Monstera plants prefer consistently moist soil but not waterlogged. Allow the top inch of soil to dry out between waterings. Overwatering can lead to root rot.  \u2028\u2028Temperature and Humidity: They prefer warm and humid conditions. Keep them in a room with temperatures between 65-80°F (18-27°C). Regular misting or placing a tray of water nearby can help maintain humidity.\u2028\u2028Soil: Use a well-draining potting mix with organic matter. Aroid or orchid mixes work well. Repot every couple of years or when the plant outgrows its container.\u2028\u2028Propagation: Monstera plants can be propagated through stem cuttings. Cut a healthy stem with a node and root it in water or directly in soil.  \u2028\u2028Pruning: Regular pruning can help control the size and shape of the plant. Cut back unwanted stems or leaves using clean, sharp scissors.  Monstera plants are not only appreciated for their aesthetic appeal but also for their relatively easy care compared to some other tropical plants. They make excellent additions to indoor spaces and are popular choices among plant enthusiasts."
                )
            )
        )
    }
}