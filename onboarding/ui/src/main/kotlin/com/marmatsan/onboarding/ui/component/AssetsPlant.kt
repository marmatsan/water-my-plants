package com.marmatsan.onboarding.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.onboarding.ui.R

enum class AssetsPlantVariant {
    Plant1,
    Plant2,
    Plant3,
    Plant4,
}

@Composable
fun AssetsPlant(
    variant: AssetsPlantVariant,
    modifier: Modifier = Modifier,
) {
    val (resId, width, height) =
        when (variant) {
            AssetsPlantVariant.Plant1 -> {
                Triple(
                    R.drawable.assets_plant_plant1,
                    119.dp,
                    181.dp,
                )
            }

            AssetsPlantVariant.Plant2 -> {
                Triple(
                    R.drawable.assets_plant_plant2,
                    116.dp,
                    209.dp,
                )
            }

            AssetsPlantVariant.Plant3 -> {
                Triple(
                    R.drawable.assets_plant_plant3,
                    139.dp,
                    182.dp,
                )
            }

            AssetsPlantVariant.Plant4 -> {
                Triple(
                    R.drawable.assets_plant_plant4,
                    112.dp,
                    168.dp,
                )
            }
        }

    Image(
        painter =
            painterResource(
                id = resId,
            ),
        contentDescription = null,
        modifier =
            modifier.size(
                width = width,
                height = height,
            ),
        contentScale = ContentScale.Fit,
    )
}

@Preview(
    showBackground = true,
)
@Composable
private fun AssetsPlantPreview(
    @PreviewParameter(AssetsPlantPreviewParameterProvider::class)
    variant: AssetsPlantVariant,
) {
    AssetsPlant(
        variant = variant,
    )
}

private class AssetsPlantPreviewParameterProvider : PreviewParameterProvider<AssetsPlantVariant> {
    override val values = AssetsPlantVariant.entries.asSequence()
}
