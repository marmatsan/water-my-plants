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
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

enum class Design {
    One,
    Two,
    Three
}

@Composable
fun Illustration(
    modifier: Modifier = Modifier,
    design: Design
) {
    when (design) {
        Design.One -> TODO()
        Design.Two -> Box(
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
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_2_objects_left),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
                Image(
                    modifier = Modifier.weight(0.10123967f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_2_objects_center),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
                Image(
                    modifier = Modifier.weight(0.44938016f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_2_objects_right),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
            }
        }
        Design.Three -> Box(
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
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_3_objects_left),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
                Image(
                    modifier = Modifier.weight(0.10123967f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_3_objects_center),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
                Image(
                    modifier = Modifier.weight(0.44938016f),
                    painter = painterResource(id = com.marmatsan.core_ui.R.drawable.illustration_3_objects_right),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = ""
                )
            }
            Image(
                painter = painterResource(id = com.marmatsan.core_ui.R.drawable.plant_icon),
                contentScale = ContentScale.FillBounds,
                contentDescription = ""
            )
        }
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