package com.marmatsan.onboarding.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.onboarding.ui.R

enum class AssetsShapeVariant {
    Shape1,
    Shape2,
    Shape3
}

@Composable
fun AssetsShape(
    variant: AssetsShapeVariant,
    modifier: Modifier = Modifier,
    tintColor: Color? = null
) {
    val colorFilter = tintColor?.let { ColorFilter.tint(it) }
    val (resId, width, height) =
        when (variant) {
            AssetsShapeVariant.Shape1 -> {
                Triple(
                    R.drawable.assets_shape_shape1,
                    95.154.dp,
                    69.31.dp
                )
            }

            AssetsShapeVariant.Shape2 -> {
                Triple(
                    R.drawable.assets_shape_shape2,
                    89.dp,
                    42.dp
                )
            }

            AssetsShapeVariant.Shape3 -> {
                Triple(
                    R.drawable.assets_shape_shape3,
                    177.dp,
                    107.dp
                )
            }
        }

    Image(
        painter =
            painterResource(
                id = resId
            ),
        contentDescription = null,
        modifier =
            modifier.size(
                width = width,
                height = height
            ),
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter
    )
}

@Preview(
    showBackground = true
)
@Composable
private fun AssetsShapePreview(
    @PreviewParameter(AssetsShapePreviewParameterProvider::class)
    variant: AssetsShapeVariant
) {
    AssetsShape(
        variant = variant
    )
}

private class AssetsShapePreviewParameterProvider : PreviewParameterProvider<AssetsShapeVariant> {
    override val values = AssetsShapeVariant.entries.asSequence()
}
