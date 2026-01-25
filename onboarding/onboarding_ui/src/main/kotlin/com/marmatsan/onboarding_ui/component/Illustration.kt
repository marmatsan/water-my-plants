package com.marmatsan.onboarding_ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.onboarding_ui.R

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
        IllustrationVariant.Illustration1 -> Illustration1(modifier)
        IllustrationVariant.Illustration2 -> Illustration2(modifier)
        IllustrationVariant.Illustration3 -> Illustration3(modifier)
    }
}

@Composable
private fun Illustration1(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(319.dp)
            .height(211.dp)
    ) {
        Plant(
            resId = R.drawable.assets_plant_plant1,
            modifier = Modifier
                .size(width = 119.dp, height = 181.dp)
                .align(Alignment.Center)
                .offset(x = (-99.5).dp, y = (-14.73).dp)
        )
        Plant(
            resId = R.drawable.assets_plant_plant3,
            modifier = Modifier
                .size(width = 139.dp, height = 182.dp)
                .align(Alignment.Center)
                .offset(x = 89.5.dp, y = (-14.23).dp)
        )
        Plant(
            resId = R.drawable.assets_plant_plant2,
            modifier = Modifier
                .size(width = 116.dp, height = 209.dp)
                .align(Alignment.Center)
                .offset(x = (-3.5).dp, y = 0.5.dp)
        )
    }
}

@Composable
private fun Illustration2(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(484.dp)
            .height(98.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.assets_shape_shape1),
            contentDescription = null,
            modifier = Modifier
                .size(width = 95.154.dp, height = 69.31.dp)
                .align(Alignment.TopStart),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_dots_dots1),
            contentDescription = null,
            modifier = Modifier
                .size(width = 63.844.dp, height = 43.622.dp)
                .align(Alignment.TopStart)
                .offset(x = 107.dp, y = 31.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_leaf_leaf1),
            contentDescription = null,
            modifier = Modifier
                .size(width = 50.dp, height = 87.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_dots_dots2),
            contentDescription = null,
            modifier = Modifier
                .size(width = 69.dp, height = 74.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-106).dp, y = 11.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_shape_shape2),
            contentDescription = null,
            modifier = Modifier
                .size(width = 89.dp, height = 42.dp)
                .align(Alignment.TopEnd),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun Illustration3(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(484.dp)
            .height(98.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.assets_leaf_leaf2),
            contentDescription = null,
            modifier = Modifier
                .size(width = 122.dp, height = 83.dp)
                .align(Alignment.TopStart)
                .offset(y = 16.93.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_dots_dots2),
            contentDescription = null,
            modifier = Modifier
                .size(width = 69.dp, height = 74.dp)
                .align(Alignment.TopStart)
                .offset(x = 129.dp, y = 7.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_dots_dots1),
            contentDescription = null,
            modifier = Modifier
                .size(width = 63.844.dp, height = 43.622.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-155.16).dp, y = 40.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.assets_leaf_leaf3),
            contentDescription = null,
            modifier = Modifier
                .size(width = 167.11.dp, height = 116.86.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 18.11.dp, y = 33.86.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun Plant(
    resId: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
private fun IllustrationPreview(
    @PreviewParameter(IllustrationPreviewParameterProvider::class)
    variant: IllustrationVariant
) {
    Illustration(variant = variant)
}

private class IllustrationPreviewParameterProvider : PreviewParameterProvider<IllustrationVariant> {
    override val values = IllustrationVariant.entries.asSequence()
}
