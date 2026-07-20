package com.marmatsan.onboarding.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Background(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(
                width = 484.dp,
                height = 988.dp
            )
            .alpha(
                alpha = 0.6f
            )
    ) {
        AssetsShape(
            variant = AssetsShapeVariant.Shape1,
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 0.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf1,
            modifier = Modifier
                .offset(
                    x = 217.dp,
                    y = 0.dp
                )
        )
        AssetsShape(
            variant = AssetsShapeVariant.Shape2,
            modifier = Modifier
                .offset(
                    x = 395.dp,
                    y = 0.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf2,
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 213.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf3,
            modifier = Modifier
                .offset(
                    x = 337.dp,
                    y = 227.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf5,
            modifier = Modifier
                .offset(
                    x = 39.dp,
                    y = 786.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf4,
            modifier = Modifier
                .offset(
                    x = 323.dp,
                    y = 823.dp
                )
        )
        AssetsShape(
            variant = AssetsShapeVariant.Shape3,
            modifier = Modifier
                .offset(
                    x = 307.dp,
                    y = 881.dp
                )
        )
        AssetsShape(
            variant = AssetsShapeVariant.Shape2,
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 946.dp
                )
                .rotate(
                    degrees = 180f
                )
        )
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun BackgroundPreview() {
    Background(
        modifier = Modifier
    )
}
