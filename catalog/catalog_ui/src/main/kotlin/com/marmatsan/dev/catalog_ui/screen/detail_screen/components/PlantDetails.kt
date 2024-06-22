package com.marmatsan.dev.catalog_ui.screen.detail_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_ui.extension.toFormattedString
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun PlantDetails(
    modifier: Modifier = Modifier,
    plant: Plant? = null
) {
    Row(
        modifier = modifier
            .clip(shapes.small)
            .alpha(0.9f)
            .background(colorScheme.surface)
            .padding(
                all = spacing.medium
            )
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top,
    ) {
        val colorScheme = colorScheme

        PlantDetailsLabel(
            modifier = Modifier
                .wrapContentSize()
                .weight(0.33f),
            text1 = "Watering days",
            text2 = plant?.wateringDays?.toFormattedString() ?: String.Empty
        )
        VerticalDivider(
            thickness = 1.dp,
            color = colorScheme.onSurface
        )
        PlantDetailsLabel(
            modifier = Modifier
                .wrapContentSize()
                .weight(0.33f),
            text1 = "Watering time",
            text2 = plant?.wateringTime?.toString() ?: String.Empty
        )
        VerticalDivider(
            thickness = 1.dp,
            color = colorScheme.onSurface
        )
        PlantDetailsLabel(
            modifier = Modifier
                .wrapContentSize()
                .weight(0.33f),
            text1 = "Water amount",
            text2 = if (plant?.waterAmount.isNotNull()) "${plant?.waterAmount} ml" else String.Empty
        )
    }
}

@Composable
private fun PlantDetailsLabel(
    modifier: Modifier = Modifier,
    text1: String,
    text2: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.small, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text1,
            textAlign = TextAlign.Center,
            style = typography.labelLarge
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            text = text2,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = typography.bodyMedium
        )
    }
}

@Preview(
    widthDp = 484
)
@Composable
private fun PlantDetailsPreview() {
    WaterMyPlantsTheme {
        PlantDetails(
            plant = Plant(
                wateringDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                wateringTime = LocalTime.of(18, 0),
                waterAmount = 500
            )
        )
    }
}