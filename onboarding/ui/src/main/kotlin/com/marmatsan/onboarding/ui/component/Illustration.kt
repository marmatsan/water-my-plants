package com.marmatsan.onboarding.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.core.ui.theme.IllustrationRefColors

enum class IllustrationVariant {
    Illustration1,
    Illustration2,
    Illustration3
}

@Composable
fun Illustration(
    variant: IllustrationVariant,
    modifier: Modifier = Modifier
) {
    when (variant) {
        IllustrationVariant.Illustration1 -> Illustration1(
            modifier = modifier
        )

        IllustrationVariant.Illustration2 -> Illustration2(
            modifier = modifier
        )

        IllustrationVariant.Illustration3 -> Illustration3(
            modifier = modifier
        )
    }
}

@Composable
private fun Illustration1(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(
            width = 319.dp,
            height = 211.dp
        )
    ) {
        AssetsPlant(
            variant = AssetsPlantVariant.Plant1,
            modifier = Modifier
                .align(
                    alignment = Alignment.Center
                )
                .offset(
                    x = (-99.5).dp,
                    y = (-14.73).dp
                )
        )
        AssetsPlant(
            variant = AssetsPlantVariant.Plant3,
            modifier = Modifier
                .align(
                    alignment = Alignment.Center
                )
                .offset(
                    x = 89.5.dp,
                    y = (-14.23).dp
                )
        )
        AssetsPlant(
            variant = AssetsPlantVariant.Plant2,
            modifier = Modifier
                .align(
                    alignment = Alignment.Center
                )
                .offset(
                    x = (-3.5).dp,
                    y = 0.5.dp
                )
        )
    }
}

@Composable
private fun Illustration2(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(
            width = 484.dp,
            height = 98.dp
        )
    ) {
        AssetsShape(
            variant = AssetsShapeVariant.Shape1,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopStart
                )
        )
        AssetsDots(
            variant = AssetsDotsVariant.Dots1,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopStart
                )
                .offset(
                    x = 107.dp,
                    y = 31.dp
                )
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf1,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopCenter
                )
        )
        AssetsDots(
            variant = AssetsDotsVariant.Dots2,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopEnd
                )
                .offset(
                    x = (-106).dp,
                    y = 11.dp
                )
        )
        AssetsShape(
            variant = AssetsShapeVariant.Shape2,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopEnd
                )
        )
    }
}

@Composable
private fun Illustration3(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(
            width = 484.dp,
            height = 98.dp
        )
    ) {
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf2,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopStart
                )
                .offset(
                    y = 16.93.dp
                )
        )
        AssetsDots(
            variant = AssetsDotsVariant.Dots2,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopStart
                )
                .offset(
                    x = 129.dp,
                    y = 7.dp
                ),
            tintColor = IllustrationRefColors.neutral60
        )
        AssetsDots(
            variant = AssetsDotsVariant.Dots1,
            modifier = Modifier
                .align(
                    alignment = Alignment.TopEnd
                )
                .offset(
                    x = (-155.16).dp,
                    y = 40.dp
                ),
            tintColor = IllustrationRefColors.teal70
        )
        AssetsLeaf(
            variant = AssetsLeafVariant.Leaf3,
            modifier = Modifier
                .align(
                    alignment = Alignment.BottomEnd
                )
                .offset(
                    x = 18.11.dp,
                    y = 33.86.dp
                )
        )
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun IllustrationPreview(
    @PreviewParameter(IllustrationPreviewParameterProvider::class)
    variant: IllustrationVariant
) {
    Illustration(
        variant = variant
    )
}

private class IllustrationPreviewParameterProvider : PreviewParameterProvider<IllustrationVariant> {
    override val values = IllustrationVariant.entries.asSequence()
}