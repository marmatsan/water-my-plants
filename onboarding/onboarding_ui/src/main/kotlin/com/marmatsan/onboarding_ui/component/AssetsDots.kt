package com.marmatsan.onboarding_ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.marmatsan.onboarding_ui.R

enum class AssetsDotsVariant {
    Dots1,
    Dots2
}

@Composable
fun AssetsDots(
    variant: AssetsDotsVariant,
    modifier: Modifier = Modifier,
    tintColor: Color? = null
) {
    val colorFilter = tintColor?.let { ColorFilter.tint(it) }
    val (resId, width, height) = when (variant) {
        AssetsDotsVariant.Dots1 -> Triple(
            R.drawable.assets_dots_dots1,
            63.844.dp,
            43.622.dp
        )
        AssetsDotsVariant.Dots2 -> Triple(
            R.drawable.assets_dots_dots2,
            69.dp,
            74.dp
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
private fun AssetsDotsPreview(
    @PreviewParameter(AssetsDotsPreviewParameterProvider::class)
    variant: AssetsDotsVariant
) {
    AssetsDots(
        variant = variant
    )
}

private class AssetsDotsPreviewParameterProvider :
    PreviewParameterProvider<AssetsDotsVariant> {
    override val values = AssetsDotsVariant.entries.asSequence()
}