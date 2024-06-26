package com.marmatsan.dev.core_ui.components.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.preview.EnumParameterProvider
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing
import kotlin.reflect.KClass

enum class IllustrationDesign {
    One,
    Two,
    Three,
    Four
}

@Composable
fun Illustration(
    modifier: Modifier = Modifier,
    illustrationDesign: IllustrationDesign
) {
    when (illustrationDesign) {
        IllustrationDesign.One -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(space = (-26).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.weight(0.33f),
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_1_objects_left),
                contentDescription = null
            )
            Image(
                modifier = Modifier.weight(0.33f),
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_1_objects_center),
                contentDescription = null
            )
            Image(
                modifier = Modifier.weight(0.33f),
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_1_objects_right),
                contentDescription = null
            )
        }

        IllustrationDesign.Two ->
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier.weight(0.33f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_2_objects_left),
                    contentScale = ContentScale.None,
                    contentDescription = null
                )
                Image(
                    modifier = Modifier.weight(0.33f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_2_objects_center),
                    contentScale = ContentScale.None,
                    contentDescription = null
                )
                Image(
                    modifier = Modifier.weight(0.33f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_2_objects_right),
                    contentScale = ContentScale.None,
                    contentDescription = null
                )
            }

        IllustrationDesign.Three -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.weight(0.5f),
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_3_objects_left),
                contentScale = ContentScale.None,
                contentDescription = null
            )
            Image(
                modifier = Modifier.weight(0.5f),
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_3_objects_right),
                contentScale = ContentScale.None,
                contentDescription = null
            )
        }

        IllustrationDesign.Four -> Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(all = spacing.none),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier.weight(0.44938016f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_4_objects_left),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
                Image(
                    modifier = Modifier.weight(0.10123967f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_4_objects_center),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
                Image(
                    modifier = Modifier.weight(0.44938016f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.figma_illustration_4_objects_right),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
            }
            Image(
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.plant_icon),
                contentScale = ContentScale.FillBounds,
                contentDescription = null
            )
        }
    }
}

private class IllustrationDesignParameterProvider : EnumParameterProvider<IllustrationDesign>() {
    override val enumClass: KClass<IllustrationDesign> = IllustrationDesign::class
}

@Preview(
    widthDp = 484,
    heightDp = 458
)
@Composable
private fun IllustrationPreview(
    @PreviewParameter(IllustrationDesignParameterProvider::class) illustrationDesign: IllustrationDesign
) {
    WaterMyPlantsTheme {
        Illustration(
            illustrationDesign = illustrationDesign
        )
    }
}