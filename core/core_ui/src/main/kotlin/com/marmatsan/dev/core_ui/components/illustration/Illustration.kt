package com.marmatsan.dev.core_ui.components.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

enum class Design {
    One,
    Two,
    Three,
    /*
    Four*/
}

@Composable
fun Illustration(
    modifier: Modifier = Modifier,
    design: Design
) {
    when (design) {
        Design.One -> Row(
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

        Design.Two ->
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

        Design.Three -> Row(
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
        /*
                Design.Four -> Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(all = spacing.default),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier.weight(0.44938016f),
                            painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_4_objects_left),
                            contentScale = ContentScale.FillBounds,
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier.weight(0.10123967f),
                            painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_4_objects_center),
                            contentScale = ContentScale.FillBounds,
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier.weight(0.44938016f),
                            painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_4_objects_right),
                            contentScale = ContentScale.FillBounds,
                            contentDescription = null
                        )
                    }
                    Image(
                        painter = painterResource(id = com.marmatsan.core_ui.R.drawable.plant_icon),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null
                    )
                }*/
    }
}

@Preview(
    widthDp = 484,
    heightDp = 458
)
@Composable
private fun IllustrationOnePreview() {
    WaterMyPlantsTheme {
        Illustration(
            design = Design.One
        )
    }
}


@Preview(
    widthDp = 484,
    heightDp = 458
)
@Composable
private fun IllustrationTwoPreview() {
    WaterMyPlantsTheme {
        Illustration(
            design = Design.Two
        )
    }
}


@Preview(
    widthDp = 484,
    heightDp = 458
)
@Composable
private fun IllustrationThreePreview() {
    WaterMyPlantsTheme {
        Illustration(
            design = Design.Three
        )
    }
}

/*
@Preview(
    widthDp = 484,
    heightDp = 524
)
@Composable
private fun IllustrationFourPreview() {
    WaterMyPlantsTheme {
        Illustration(
            design = Design.Four
        )
    }
}*/
