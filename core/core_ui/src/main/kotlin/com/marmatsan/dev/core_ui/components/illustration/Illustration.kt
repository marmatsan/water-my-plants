package com.marmatsan.dev.core_ui.components.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_ui.theme.LocalSpacing
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun Illustration(
    modifier: Modifier = Modifier,
    design: Design
) {
    val spacing = LocalSpacing.current

    when (design) {
        Design.One -> TODO()
        Design.Two -> TODO()
        Design.Three -> Row(
            modifier = modifier.padding(all = spacing.default),
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
    }
}

@Preview(
    widthDp = 100,
    heightDp = 50
)
@Composable
private fun IllustrationPreview() {
    WaterMyPlantsTheme {
        Illustration(
            design = Design.Three
        )
    }
}