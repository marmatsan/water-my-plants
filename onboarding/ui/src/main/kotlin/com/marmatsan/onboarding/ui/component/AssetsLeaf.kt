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

enum class AssetsLeafVariant {
    Leaf1,
    Leaf2,
    Leaf3,
    Leaf4,
    Leaf5
}

@Composable
fun AssetsLeaf(
    variant: AssetsLeafVariant,
    modifier: Modifier = Modifier,
    tintColor: Color? = null
) {
    val colorFilter = tintColor?.let { ColorFilter.tint(it) }
    val (resId, width, height) = when (variant) {
        AssetsLeafVariant.Leaf1 -> Triple(
            R.drawable.assets_leaf_leaf1,
            50.dp,
            87.dp
        )
        AssetsLeafVariant.Leaf2 -> Triple(
            R.drawable.assets_leaf_leaf2,
            122.dp,
            83.dp
        )
        AssetsLeafVariant.Leaf3 -> Triple(
            R.drawable.assets_leaf_leaf3,
            167.11.dp,
            116.86.dp
        )
        AssetsLeafVariant.Leaf4 -> Triple(
            R.drawable.assets_leaf_leaf4,
            109.839.dp,
            111.949.dp
        )
        AssetsLeafVariant.Leaf5 -> Triple(
            R.drawable.assets_leaf_leaf5,
            98.107.dp,
            124.907.dp
        )
    }

    Image(
        painter = painterResource(
            id = resId
        ),
        contentDescription = null,
        modifier = modifier.size(
            width = width,
            height = height
        ),
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter
    )
}

@Preview(showBackground = true)
@Composable
private fun AssetsLeafPreview(
    @PreviewParameter(AssetsLeafPreviewParameterProvider::class)
    variant: AssetsLeafVariant
) {
    AssetsLeaf(
        variant = variant
    )
}

private class AssetsLeafPreviewParameterProvider :
    PreviewParameterProvider<AssetsLeafVariant> {
    override val values = AssetsLeafVariant.entries.asSequence()
}